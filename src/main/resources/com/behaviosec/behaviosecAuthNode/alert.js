!function (window, document) {
	alert("ping");
	window.addEventListener('DOMContentLoaded', function () {
		console.log('window - DOMContentLoaded - capture'); // 1st
	}, true);
	document.addEventListener('DOMContentLoaded', function () {
		console.log('document - DOMContentLoaded - capture'); // 2nd
	}, true);
	document.addEventListener('DOMContentLoaded', function () {
		console.log('document - DOMContentLoaded - bubble'); // 2nd
	});
	window.addEventListener('DOMContentLoaded', function () {
		console.log('window - DOMContentLoaded - bubble'); // 3rd
	});
}(window, document);