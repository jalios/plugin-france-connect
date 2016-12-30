/**
 * -----------------------------------------------------------------
 *
 *  Plugin's JavaScript sample.
 * 
 *  To include this file :
 *   - in a JSP : jcmsContext.addJavaScript("plugins/{Name}/js/plugin.js");
 *   - or in Java : implements PortalPolicyFilter.setupHeaders()
 * 
 *  You should use JSLint (http://www.jslint.com/) to ensure 
 *  you have a nice and clean JavaScript code.
 * 
 *  More information at :
 *  - http://jqueryboilerplate.com/
 *  - http://docs.jquery.com/Plugins/Authoring
 *  - See Bootstrap's code sample for advanced object wrapper
 * 
 * -----------------------------------------------------------------
 */ 

!function ($) {

  // ------------------------------------------
  //  PRIVATE CODE
  // ------------------------------------------

  var privateFunction = function(){ /*...*/ }
  var privateVariable = 'Hello World';
  
  /* Do stuff on DOM Ready */
  var register = function(){
  
    // Register click callback
    $(document).on('click', 'A.clickMe', function(event){
      $.console.log('Hello World');
    }); 
    
    // Register Ajax-Refresh callback
    $(document).on('jalios:refresh', callback);
  }
  
  /* Handle Ajax-Refresh Event */
  var callback = function(event){
  
    // Handle an Ajax-Refresh After
    var refresh = $.jalios.Event.match(event, 'refresh', 'after');
    if (!refresh || !refresh.target){ return; }
    
    // Work on refreshed content
    refresh.target.each(function(){ /* ... */ });
  }
  
  // ------------------------------------------
  //  PUBLIC CODE
  // ------------------------------------------

  // Namespace
  if (!$.plugin) { $.plugin = {}; }
  
  /**
   * Documentation of the MyPlugin JavaScript class.<br/>
   * <br/>
   * <h5>Description</h5>
   * 
   * @namespace $.plugin.MyPlugin
   */
  $.plugin.MyPlugin = {
    
    /**
     * Documentation of the publicFunction
     * @param {String} param1 the param1 description
     * 
     * @example
     * 
     * var value = $.plugin.MyPlugin.publicFunction(privateVariable);
     */
    publicFunction: function(param1){ /*...*/ },
    
    /**
     * Documentation of the otherFunction
     */
    otherFunction: function(){ 
      return this.each(function() { var $this = $(this); /*...*/  });
    }
  }
  
  // ------------------------------------------
  //  JQUERY FUNCTION CODE
  // ------------------------------------------
  
  // Bind otherFunction to $.fn to be available in jQuery selection
  $.fn.otherFunction = $.plugin.MyPlugin.otherFunction;

  // ------------------------------------------
  //  DOM READY CODE
  // ------------------------------------------
  
  $(document).ready(function() {
    register();
  });

}(window.jQuery);


  // ------------------------------------------
  //  EVERYWHERE ELSE
  // ------------------------------------------

  // Do not use $ because of noConflict() with Prototype
  // jQuery.plugin.MyPlugin.publicFunction(); 

  // To do things on DOMReady in HTML Page use jalios tag
  // <jalios:javascript>
  //   jQuery('#page .myplugin').otherFunction();
  // </jalios:javascript>

 