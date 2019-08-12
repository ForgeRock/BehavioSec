callFunction = function (callback) {
	var info = {},
	successCallback = function(param1) {
	        document.getElementById("behaviosecoutput").value = "1";
	        callback(info);
	    }, 
    errorCallback = function(error) {
        document.getElementById("behaviosecoutput").value = "2";       
         callback(info);
    };
};

let param = '1';
autoSubmitDelay = 30000;
console.log("DEBUG");
document.getElementById("behaviosecoutput").value = callFunction(function(param) {
	document.getElementById("behaviosecoutput").value = 'test1';
	document.getElementById("loginButton_0").click();
});
  
  
  
  
