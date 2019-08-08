collectGeolocationInfo = function (callback) {
    var geolocationInfo = {},
    successCallback = function(position) {
        geolocationInfo.longitude = position.coords.longitude;
        geolocationInfo.latitude = position.coords.latitude;
        callback(geolocationInfo);
    }, 
    errorCallback = function(error) {
        console.warn("Cannot collect geolocation information. " + error.code + ": " + error.message);
        callback(geolocationInfo);
    };
    if (navigator && navigator.geolocation) {
        // NB: If user chooses 'Not now' on Firefox neither callback gets called
        //     https://bugzilla.mozilla.org/show_bug.cgi?id=675533
        navigator.geolocation.getCurrentPosition(successCallback, errorCallback);
    } else {
        console.warn("Cannot collect geolocation information. navigator.geolocation is not defined.");
        callback(geolocationInfo);
    }
  };
  
  autoSubmitDelay = 30000;
  console.log("DEBUG");
  collectGeolocationInfo(function(geolocationInfo) {
      output.value = JSON.stringify(geolocationInfo);
      submit();
  });