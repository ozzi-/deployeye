'use strict';

// bug where after redeploy user is navigated to frontend// which causes load loop
// because the xhr use ../ which then is wrong . . . 
var fullURL = window.location.href;
if(fullURL.indexOf('frontend//')!==-1){
	window.location.replace(fullURL.replace("frontend//","frontend/"));
}

var page = getQueryParams(document.location.search).page;
if(page === undefined || page == "undefined" || page == "index" ){
	page = "main";
}
page = page.replace(/[^A-Za-z0-9]/g,'');

document.addEventListener("DOMContentLoaded", function(event) {
	doRequest("GET", page+".html", pageLogic, []);
});

var refreshInterval;

function pageLogic (response){
	
	var breadcrumbs = [];	
	document.getElementById("pageContainer").innerHTML = response;

	breadcrumbs.push(["Home","main"]);
	
	flatpickr('input[type="datetime-local"]', {
		enableTime: true,
		altInput: true,
		altFormat: 'd.m.Y H:i',
		dateFormat: 'Y-m-dTH:i:S',
		time_24hr: true,
		allowInput: true
	});
	
	if(refreshInterval !== undefined){
		clearInterval(refreshInterval);	
	}

	if(page=="main"){
		doRequest("GET", "../eye", loadEyeOverview);
		refreshInterval =  setInterval(function() {
			doRequest("GET", "../eye", loadEyeOverview);
		}, 5000);
	}else if(page=="eye"){
		var id = getQueryParams(document.location.search).id;
		breadcrumbs.push(["Eye ["+id+"]","eye"]);

		doRequest("GET", "../eye/"+id, loadEye, [true]);
		refreshInterval =  setInterval(function() {
			doRequest("GET", "../eye/"+id, loadEye, [false]);
		}, 5000);
		
	}
	
	renderBreadCrumbs(breadcrumbs);
}