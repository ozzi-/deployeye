'use strict';

var colorAccent3 = "#773da6";
var badgeClass = "badge";
var badgeSuccessClass = "badge-success";

window.onerror = function(msg, url, line, col, error) {
	showAlert("Something went wrong","\""+error+"\"","error");
};


function showAlert(title,text,type){
	Swal.fire({
		title: title,
		text: text,
		icon: type,
		confirmButtonText: 'OK'
	})	
}

function renderBreadCrumbs(crumbs){
	var breadCrumbs = document.getElementById("breadcrumbOl");
	if(breadCrumbs==null){
		console.log("Error: No bread crumbs ol")
		return;
	}
	var crumbsString = "";
	for (var i = 0; i < crumbs.length; i++) {		
		crumbs[i][1] = crumbs[i][1]==null?"javascript:;":"index.html?page="+crumbs[i][1];
		if(i==crumbs.length-1){
			crumbsString += '<li class="breadcrumb-item active" aria-current="page">'+escapeHtml(crumbs[i][0])+'</li>';
		}else{
			crumbsString += '<li class="breadcrumb-item"><a href="'+crumbs[i][1]+'">'+escapeHtml(crumbs[i][0])+'</a></li>';			
		}
	}
	breadCrumbs.innerHTML = crumbsString;
}



function custFormatter (cell, formatterParams, onRendered){
	return unescape(cell.getValue());
}


function goTo(response,url){
	window.location.replace('index.html'+url);
}

function matchAny(data, filterParams){
    var match = false;
    var obj = filterParams[0];
    var filterColumnsStartingWithArray = filterParams[1];
    // empty search string means all match
    if(obj.value==""){
    	return true;
    }
   
	var searchTermRaw = obj.value.toLowerCase();
	var searchTerms = searchTermRaw.split(" ");
	var searchTermsCount = 0;
	var matches = 0;
	
	for (var i = 0; i < searchTerms.length; i++) {
		var alreadyMatchedSearchTerm = false;
		var searchTerm = searchTerms[i];
		if(searchTerm!="" && searchTerm!=" "){
			searchTermsCount++;
			for(var key in data){
				var skip = false;
				if(Array.isArray(filterColumnsStartingWithArray)){
					for (var ii = 0; ii < filterColumnsStartingWithArray.length; ii++) {
						var filterColumnsStartingWith = filterColumnsStartingWithArray[ii];
						if(filterColumnsStartingWith!==null){
							if(key.startsWith(filterColumnsStartingWith)){
								skip=true;
								continue;
							}    		
						}
					}
					if(skip){
						continue;
					}
				}
		    	var value = String(data[key]).toLowerCase();
		    	if(value.includes(searchTerm)){
		    		if(!alreadyMatchedSearchTerm){
		    			matches ++;
		    			// only one match per term, as otherwise "roger sch"
						// might match many entries containing "sch" multiple
						// times without containing "roger"
		    			alreadyMatchedSearchTerm=true;
		    		}
		    	}
		    }
		}
	}  
    return matches>=searchTermsCount;
}

function updateFilter(id, table, filterColumnsStartingWith){
	var filterValueField = document.getElementById(id);
    table.setFilter(matchAny, [filterValueField,filterColumnsStartingWith]);
}


function delay(callback, ms) {
	var timer = 0;
	return function() {
		var context = this, args = arguments;
		clearTimeout(timer);
	    timer = setTimeout(function () {
	    	callback.apply(context, args);
	    }, ms || 0);
	};
}

function runInternal(res, name, paramName, call){
	var pollTime = 1200;
	var handle = res.handle;
	function doPoll() {
		doRequest("GET", "../"+call+"/" + name + "/" + handle, poll, [name,paramName, poller, handle], true);
	}
	doPoll();
	var poller;
	poller = setInterval(doPoll, pollTime);
}

// *******************
// * JS Elem Builder *
// *******************

function appendHREF(url,text,appendTo){
	var a = document.createElement('a');
	a.setAttribute('href',url);
	a.text = text;	
	appendTo.appendChild(a);
}

function appendButton(text, className, onClickFunc, appendTo){
    var button = document.createElement('button');
    button.textContent=text;
	button.classList.add(className);
	button.classList.add("btn");
	button.style.marginRight="6px";
	button.addEventListener('click', onClickFunc, false);

	appendTo.appendChild(button);
}

function getLocHrefFunc(href){
	return function() {
	    location.href=href;
	}
}


// ***********
// * Network *
// ***********
window.errorReported = false;
var openRequests=0;
var timeout = 10000;

setTimeout(killLoader, timeout);

function killLoader(){
	document.getElementById("loading").style.display="none";
}

function killLoaderIfNoRequestsOpen(){
	setTimeout(killLoaderIfNoRequestsOpenInt, 300);
}

function killLoaderIfNoRequestsOpenInt(){
	if(openRequests==0){
		killLoader();
	}
}


function doRequestBody(method, data, type, url, callback, params) {
	doRequestBodyInternal(method,data,type,url,callback,params);
}

function doRequest(method, url, callback, params) {
	doRequestBodyInternal(method,null,null,url,callback,params);
}

function doRequestBodyInternal(method,data,type,url,callback,params){
	openRequests++;
	var request = new XMLHttpRequest();

	request.onreadystatechange = function() {
		if (request.readyState == 4) {
			openRequests--;
			killLoaderIfNoRequestsOpen();
			if (request.status == 200) {
				var response = "";
				if(url.includes(".html")){
					response = request.responseText;
				}else{
					try { 
						var responseJSON = JSON.parse(request.responseText);
						response = responseJSON;
					} catch (e) {
						showAlert("Something went wrong","Error receiving information from backend - "+e.message,"error");
					}
				}
				params = [response].concat(params);
				params.push(request.status); 				
				callback.apply(this,params);
			} else{
				showAlert("Something went wrong",response.error,"error");
			}
		}
	};
	if(method.toUpperCase()==="GET"){
		var cacheBusterStrng = "cacheBuster=";
		if(!url.includes(cacheBusterStrng)){
			var appendChar = url.includes("?") ? "&" : "?"; 
			url = url + appendChar + cacheBusterStrng + (Math.random()*1000000);
		}
	}
	request.open(method, url);
	request.timeout = timeout;
	if(type!==null){
		request.setRequestHeader("Content-Type", type);		
	}
	request.setRequestHeader("Cache-Control", "max-age=0");

	request.setRequestHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	if(data!=null){
		request.send(data);
	}else{
		request.send();
	}
}

// ********************
// * Helper Functions *
// ********************

function escapeHtml(html){
  var text = document.createTextNode(html);
  var p = document.createElement('p');
  p.appendChild(text);
  return p.innerHTML;
}

function getQueryParams(qs) {
	qs = qs.split('+').join(' ');
	var params = {}, tokens, re = /[?&]?([^=]+)=([^&]*)/g;
	while (tokens = re.exec(qs)) {
		params[decodeURIComponent(tokens[1])] = decodeURIComponent(tokens[2]);
	}
	return params;
}