var iWebkit;
if (!iWebkit) {
  iWebkit = window.onload = function() {
    function fullscreen() {
      var a = document.getElementsByTagName("a");
      for (var i=0 ; i<a.length ; i++) {
        if (a[i].className.match("noeffect")){
          // Do nothing
        } else {
          a[i].onclick = function() {
            window.location = this.getAttribute("href");
            return false
          }
        }
      }
      function hideURLbar() {
        window.scrollTo(0,0.9)
      }
      iWebkit.init=function() {
        fullscreen();hideURLbar()
      };
      iWebkit.init()
    }
  }
}

function showHide(id, btn) {
  var e = document.getElementById(id);
  if (e.style.display == 'none') {
    e.styledisplay = 'block';
  } else {
    e.style.display = 'none';
  }
}


function getBaseURL() {
  var url = location.href;
  var baseURL = url.substring(0, url.lastIndexOf('/'));
  if (baseURL.indexOf('http://localhost') != -1) {
    var url = location.href;var pathname = location.pathname;var index1 = url.indexOf(pathname);
    var index2 = url.indexOf("/", index1 + 1);var baseLocalUrl = url.substr(0, index2);
    return baseLocalUrl + "/";
  } else {
    return baseURL + "/";
  }
}
