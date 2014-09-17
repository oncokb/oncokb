<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8"/>
    <link type="text/css" rel="stylesheet" href="resources/css/bootstrap.css?09162014-1"/>
    <link type="text/css" rel="stylesheet" href="resources/css/style.css?09162014-1"/>
    <link rel="stylesheet" type="text/css" href="resources/css/jquery.qtip.min.css?09162014-1">
    <script type="text/javascript" src="resources/js/lib/jquery-1.11.1.min.js?09162014-1"></script>
    <script type="text/javascript" src="resources/js/lib/bootstrap.js?09162014-1"></script>
    <script type="text/javascript" src="resources/js/lib/jquery.qtip.min.js?09162014-1"></script>
    <script type="text/javascript" src="http://d3js.org/d3.v3.min.js?09162014-1"></script>
    <script type="text/javascript" src="resources/js/src/main.js?09162014-1"></script>
    <script type="text/javascript" src="resources/js/src/DataProxy.js?09162014-1"></script>
    <script type="text/javascript" src="resources/js/src/tree.js?09162014-1"></script>
  </head>

  <body>
    <div id="body" class="container">
      <div class="row">
        <div class="col-lg-9 col-md-9 col-sm-12 col-xs-12">
          <b>OncoKB Tree</b>
          <br/>
          <!--<span id="summary-info"></span>-->
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
            <button type="button" class="btn btn-default active" id='combined-variants-btn'>
              <span>Combined Variants</span>
            </button>
            <button type="button" class="btn btn-default" id='separated-variants-btn'>
              <span>Separated Variants</span>
            </button>
              
            <div class="btn-group">
                <button type="button" class="btn btn-default glyphicon glyphicon-resize-full" id='expand-nodes-btn'></button>
                <button type="button" class="btn btn-default glyphicon glyphicon-resize-small" id='collapse-nodes-btn'></button>
            </div>
          </div>
        </div>
      </div>
      
      <div class="row">
          <img id='tree_loader' src="resources/img/ajax-loader.gif"/>
          <div id="tree" class="_hidden"></div>
      </div>

      <span id="top-link-block" class="hidden">
        <a href="#top" class="well well-sm"  onclick="$('html,body').animate({scrollTop:0, scrollLeft: 0},'slow');return false;">
            <i class="glyphicon glyphicon-chevron-up"></i> Back to Top
        </a>
      </span>
    </div>
  </body>
</html>

