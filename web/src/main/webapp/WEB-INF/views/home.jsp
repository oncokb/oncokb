<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8"/>
    <link type="text/css" rel="stylesheet" href="resources/css/bootstrap.css"/>
    <link rel="stylesheet" type="text/css" href="resources/css/jquery.qtip.min.css">

    <link type="text/css" rel="stylesheet" href="resources/css/style.css?version=10142014-1"/>

    <script  data-main="resources/js/src/main.js?version=10142014-1" src="resources/js/lib/require.js" type="text/javascript"></script>
</head>

  <body>
    <div id="displayTabs" class="container">
        <ul class="nav nav-tabs" role="tablist">
            <li id="tab-1"><a href="#mainTree" data-toggle="tab">Home</a></li>
            <li id="tab-2"><a href="#variantDisplay" data-toggle="tab">Variants Annotation</a></li>
        </ul>
        <div  class="tab-content">
      <div id="mainTree" class="tab-pane fade in">
        <div class="row">
          <div class="col-lg-9 col-md-9 col-sm-12 col-xs-12">
            <br/>
            <b>OncoKB Tree</b>
            <br/>
          </div>
        </div>
        <br />
        <div class="row">
          <div class="col-lg-4 col-md-6 col-sm-12 col-xs-12 has-addon-feedback">
            <div class="input-group" id='tumor_search'>
              <input type="text" class="form-control" placeholder="Search Term" />
              <span id="searchRemoveIcon" class="form-control-feedback glyphicon glyphicon-remove-circle"></span>
              <span id="searchResult" class="form-control-feedback result"></span>
              <span class="input-group-btn">
                <button type="button" class="btn btn-default">Search</button>
              </span>

            </div>
          </div>

          <div class="col-lg-8 col-md-6 col-sm-12 col-xs-12">
            <div>
              <button type="button" class="btn btn-default active" id='separated-variants-btn'>
                <span>Separated Variants</span>
              </button>
              <button type="button" class="btn btn-default" id='combined-variants-btn'>
                <span>Combined Variants</span>
              </button>

              <div class="btn-group">
                  <button type="button" class="btn btn-default glyphicon glyphicon-resize-full" id='expand-nodes-btn'></button>
                  <button type="button" class="btn btn-default glyphicon glyphicon-resize-small" id='collapse-nodes-btn'></button>
              </div>
            </div>
          </div>
        </div>
         <div class="row">
          <div class="col-lg-4 col-md-6 col-sm-12 col-xs-12 has-addon-feedback">
                  <img id='tree_loader' src="resources/img/ajax-loader.gif"/>
                  <div id="tree" class="_hidden"></div>
          </div>
         </div>
      </div>
     <div id="variantDisplay" class="tab-pane fade in">
        <div class="row">
            <div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
                <h2>Search Criteria</h2>
            </div>
        </div>
        <br />
        <div class="row">
            <div class="col-lg-3 col-md-3 col-sm-6 col-xs-12">
                <div class="input-group input-group-sm">
                    <span class="input-group-addon"><b>Gene Name:</b></span>
                    <input type="text" id="variantGeneName" class="form-control" placeholder="(eg. BRAF)">
                </div>
            </div>
            <div class="col-lg-3 col-md-3 col-sm-6 col-xs-12">
                <div class="input-group input-group-sm">
                    <span class="input-group-addon"><b>Mutation:</b></span>
                    <input type="text" id="variantMutation" class="form-control" placeholder="(eg. V600E)">
                </div>
            </div>
            <div class="col-lg-3 col-md-3 col-sm-6 col-xs-12">
                <select id="tumorTypesDropDown" style="width: 150px" class="chosen-select" data-placeholder="Choose a tumor type (eg. lung cancer)">
                    <option value=""></option>
                </select>
            </div>
            <div class="col-lg-3 col-md-3 col-sm-6 col-xs-12">
                <div class="btn-group btn-group-sm">
                    <button id="searchVariantBtn" type="button" class="btn btn-default">Search</button>
                    <button id="useExampleBtn" type="button" class="btn btn-default">Use example</button>
                </div>
            </div>
        </div>
        <div class="row">
            <div>
                <img id='variant_loader' class="_hidden" src="resources/img/ajax-loader.gif"/>
            </div>
            <div id="variantDisplayResult" class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
                
            </div>
        </div>
    </div>
        </div>
      <span id="top-link-block" class="hidden">
        <a href="#top" class="well well-sm"  onclick="$('html,body').animate({scrollTop:0, scrollLeft: 0},'slow');return false;">
            <i class="glyphicon glyphicon-chevron-up"></i> Back to Top
        </a>
      </span>
    </div>
  </body>
</html>

