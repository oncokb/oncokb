angular.module('webappApp').filter('getIndexByObjectNameInArray', function() {
	'use strict';
    return function(array, attr, name) {
        for (var i = 0, arrayL = array.length; i < arrayL; i++) {
            if(array[i][attr] === name) {
                return i;
            }
        }
        return null;
    };
})
.filter('sortObject', function () {
	var delayAttrs = ['description'];
	var prioAttrs = ['trial','cancer_type','version'];

  	return function (obj, addKey) {
	    if (!(obj instanceof Object)) {
	      return obj;
	    }

	    if ( addKey === false ) {
	      return Object.values(obj);
	    } else {
	    	var keys = Object.keys(obj).filter(function(item) {
	    		return item !== '$$hashKey';
	    	}).sort(function(a,b) {
				      	if( delayAttrs.indexOf(a) !== -1){
				        	return 1;
				      	}else if (delayAttrs.indexOf(b) !== -1) {
				        	return -1;
				      	}else if (prioAttrs.indexOf(a) !== -1) {
				        	return -1;
				      	}else if(prioAttrs.indexOf(b) !== -1) {
				        	return 1;
				      	}else {
					        if(a < b) {
					          	return -1;
					        }else {
					          	return 1;
					        }
				      	}
				    });
	      	return keys.map(function (key) {
	      				if(typeof obj[key] !== 'object') {
	      					var _obj = {};
	      					Object.defineProperty(_obj, '$value', { enumerable: false, value: obj[key]});
	      					obj[key] = _obj;
	      				}
				        return Object.defineProperty(obj[key], '$key', { enumerable: false, value: key});
			      	});
	    	}
  	};
});