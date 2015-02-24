'use strict';

/**
 * @ngdoc service
 * @name oncokbApp.importer
 * @description
 * # importer
 * Service in the oncokbApp.
 */
angular.module('oncokb')
  .service('importer', function importer(documents, S, storage, OncoKB) {
    var self = this;
    self.docs = [];
    self.newFolder = '';
    self.parentFolder = '0BzBfo69g8fP6fmdkVnlOQWdpLWtHdFM4Ml9vNGxJMWpNLTNUM0lhcEc2MHhKNkVfSlZjMkk';
    function backup() {
      storage.requireAuth(true).then(function(result){
        if(result && !result.error) {
          // storage.createFolder(self.parentFolder).then(function(result){
            // if(result.id){
              // var docs = Documents.get({title: 'BRAF'});
              self.docs = documents.get();
              self.newFolder = '0BzBfo69g8fP6fmdkVnlOQWdpLWtHdFM4Ml9vNGxJMWpNLTNUM0lhcEc2MHhKNkVfSlZjMkk';
              // self.newFolder = result.id;
              copyData(0);
            // }else {
            //   console.error('Create folder failed.');
            // }
          // });
        }
      });
    }

    function copyData(index) {
      if(index < self.docs.length) {
        var doc = self.docs[index];
        copyFileData(self.newFolder, doc.id, doc.title, index, copyData);
      }else {
        console.log('finished');
      }
    }

    function getData(realtime) {
      var gene = {};
      var geneData = realtime;

      gene = combineData(gene, geneData, ['name', 'summary', 'background']);
      gene.mutations = [];
      gene.curators = [];

      geneData.curators.asArray().forEach(function(e){
        var _curator = {};
        _curator = combineData(_curator, e, ['name', 'email']);
        gene.curators.push(_curator);
      });

      geneData.mutations.asArray().forEach(function(e){
        var _mutation = {};
        _mutation = combineData(_mutation, e, ['name', 'oncogenic', 'description']);

        _mutation.effect = {};
        _mutation.tumors = [];
        _mutation.effect = combineData(_mutation.effect, e.effect, ['value', 'addOn']);

        if(e.effect_comments) {
          _mutation.effect_comments = getComments(e.effect_comments);
        }

        e.tumors.asArray().forEach(function(e1){
          var __tumor = {};

          __tumor = combineData(__tumor, e1, ['name', 'prevalence', 'progImp']);
          __tumor.trials = [];
          __tumor.TI = [];
          __tumor.nccn = {};
          __tumor.interactAlts = {};

          e1.trials.asArray().forEach(function(trial){
            __tumor.trials.push(trial);
          });
          if(e1.trials_comments) {
            __tumor.trials_comments = getComments(e1.trials_comments);
          }
          e1.TI.asArray().forEach(function(e2){
            var ti = {};

            ti = combineData(ti, e2, ['name', 'description']);
            ti.status = getString(e2.types.get('status'));
            ti.type = getString(e2.types.get('type'));
            ti.treatments = [];

            e2.treatments.asArray().forEach(function(e3){
              var treatment = {};

              treatment = combineData(treatment, e3, ['name', 'type', 'level', 'indication', 'description']);
              ti.treatments.push(treatment);
            });
            __tumor.TI.push(ti);
          });

          __tumor.nccn = combineData(__tumor.nccn, e1.nccn, ['therapy', 'disease', 'version', 'pages', 'category', 'description']);
          __tumor.interactAlts = combineData(__tumor.interactAlts, e1.interactAlts, ['alterations', 'description']);
          _mutation.tumors.push(__tumor);
        });

        gene.mutations.push(_mutation);
      });
      return gene;
    }

    function copyFileData(folderId, fileId, fileTitle, docIndex, callback) {
      storage.requireAuth(true).then(function () {
        storage.createDocument(fileTitle, folderId).then(function (file) {
          console.log('Created file ', fileTitle);
          storage.getRealtimeDocument(fileId).then(function (realtime){
            console.log('\t copying');
            var gene = realtime.getModel().getRoot().get('gene');
            if(gene) {
              var geneData = getData(gene);
              storage.getRealtimeDocument(file.id).then(function(newRealtime){
                var model = createGeneModel(newRealtime.getModel());
                var geneModel = model.getRoot().get('gene');

                model.beginCompoundOperation();
                for(var key in geneData) {
                  if(geneModel[key]) {
                    geneModel = setValue(model, geneModel, geneData[key], key);
                  }
                }
                model.endCompoundOperation();
              });
            }
            console.log('\t Done.');
            if(angular.isFunction(callback)) {
              callback(++docIndex);
            }
          });
        });
      });
    }

    function setValue(rootModel, model, value, key) {
      if(angular.isString(value)) {
        if(model[key] && model[key].type) {
          model[key].setText(value);
        }else {
          console.log('Unknown key', key);
        }
      }else if(angular.isArray(value)) {
        value.forEach(function(e){
          var _datum;
          switch(key) {
            case 'curators':
              _datum = rootModel.create('Curator');
              break;
            case 'mutations':
              _datum = rootModel.create('Mutation');
              break;
            case 'tumors':
              _datum = rootModel.create('Tumor');
              break;
            case 'TI':
              _datum = rootModel.create('TI');
              break;
            case 'treatments':
              _datum = rootModel.create('Treatment');
              break;
            case 'trials':
              _datum = e;
              break;
            default:
              _datum = rootModel.create('Comment');
              break;
          }

          if(key === 'TI') {
            _datum.types.set('status', e.status);
            _datum.types.set('type', e.type);
            delete e.status;
            delete e.type;
          }
          if(key !== 'trials') {
            for(var _key in e) {
              _datum = setValue(rootModel, _datum, e[_key], _key);
            }
          }
          model[key].push(_datum);
        });
      }else if(angular.isObject(value)) {
        var _datum;
        switch(key) {
          case 'nccn':
            _datum = rootModel.create('NCCN');
            break;
          case 'effect':
            _datum = rootModel.create('ME');
            break;
          case 'interactAlts':
            _datum = rootModel.create('InteractAlts');
            break;
        }
        for(var _key in value) {
          _datum = setValue(rootModel, _datum, value[_key], _key);
        }
        model[key] = _datum;
      }else {
        console.log('Error value type.');
      }
      return model;
    }

    function createGeneModel(model) {
      if(!model.getRoot().get('gene')) {
        var gene = model.create('Gene');
        model.getRoot().set('gene', gene);
      }
      return model;
    }

    function combineData(object, model, keys) {
      keys.forEach(function(e){
        object[e] = getString(model[e].getText());
        if(model[e + '_comments']) {
          object[e + '_comments'] = getComments(model[e + '_comments']);
        }
      });
      return object;
    }

    function getString(string){
      string = S(string).stripTags().s;
      return string;
    }

    function getComments(model) {
      var comments = [];
      var commentKeys = Object.keys(OncoKB.curateInfo.Comment);
      var comment = {};

      commentKeys.forEach(function(e){
        comment[e] = '';
      });

      model.asArray().forEach(function(e){
        var _comment = angular.copy(comment);
        for(var key in _comment) {
          if(e[key]) {
            _comment[key] = e[key].getText();
          }
        }
        comments.push(_comment);
      });
      return comments;
    }

    return {
      backup: backup,
      getData: getData
    };
  });
