<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8"/>
    <link type="text/css" rel="stylesheet" href="resources/css/bootstrap.css?09102014-2"/>
    <link type="text/css" rel="stylesheet" href="resources/css/style.css?09102014-2"/>
    <link rel="stylesheet" type="text/css" href="resources/css/jquery.qtip.min.css?09102014-2">
    <script type="text/javascript" src="resources/js/lib/jquery-1.11.1.min.js?09102014-2"></script>
    <script type="text/javascript" src="resources/js/lib/bootstrap.js?09102014-2"></script>
    <script type="text/javascript" src="resources/js/lib/jquery.qtip.min.js?09102014-2"></script>
    <script type="text/javascript" src="http://d3js.org/d3.v3.min.js?09102014-2"></script>
    <script type="text/javascript" src="resources/js/src/main.js?09102014-2"></script>
    <script type="text/javascript" src="resources/js/src/DataProxy.js?09102014-2"></script>
    <script type="text/javascript" src="resources/js/src/tree.js?09102014-2"></script>
  </head>

  <body>
    <div id="body" class="container">
      <div class="row">
        <div class="col-lg-9 col-md-9 col-sm-12 col-xs-12">
          <b>OncoKB Tree</b><br>
          Please send all comments and suggestions to <a href="mailto:schultz@cbio.mskcc.org?Subject=Oncotree comment" target="_top"><u>schultz@cbio.mskcc.org</u></a><br>
          If you cannot see the tree below in Internet Explorer, please use Firefox or Chrome.
          <br/>
          Last updated September 16, 2014.
          <span id="summary-info"></span>
        </div>
      </div>
      <br />
      <div class="row">
        <div class="col-lg-4 col-md-5 col-sm-6 col-xs-9 has-addon-feedback">
          <div class="input-group" id='tumor_search'>
            <input type="text" class="form-control" placeholder="Search Term" />
            <span id="searchRemoveIcon" class="form-control-feedback glyphicon glyphicon-remove-circle"></span>
            <span id="searchResult" class="form-control-feedback result"></span>
            <span class="input-group-btn">
              <button type="button" class="btn btn-default">Search</button>
            </span>
            
          </div>
        </div>

        <div class="col-lg-3 col-md-3 col-sm-3 col-xs-3">
          <div class="btn-group">
            <button type="button" class="btn btn-default" id='expand-nodes-btn'>
              <span class="glyphicon glyphicon-resize-full"></span>
            </button>

            <button type="button" class="btn btn-default" id='collapse-nodes-btn'>
              <span class="glyphicon glyphicon-resize-small"></span>
            </button>
          </div>
        </div>
      </div>
      
      <div>
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

