var tilesLoadedCounter = 0;

var availabilityTable;

function loadEye(eye, updateAvail){
	document.getElementById("eyeName").innerText = eye.eye.eye_name;
	document.getElementById("eyeChangelog").href = eye.eye.eye_changelog;
	document.getElementById("eyeUrl").href = eye.eye.eye_url;
	document.getElementById("eyeUrl").innerText = eye.eye.eye_url;

	var versionTable = new Tabulator("#versionTable", {
	    layout:"fitDataFill",
		layoutColumnsOnNewData:true,
		selectable:false,
	    columns:[
			{title:"Version", field:"version_string"},
			{title:"Date", field:"version_date"},
	    ]
	});
	versionTable.setData(eye.version);


	if(updateAvail){
		// TODO find a way to update the newest row only without breaking the "load more" rows
		availabilityTable = new Tabulator("#availabilityTable", {
		    layout:"fitDataFill",
			layoutColumnsOnNewData:true,
			selectable:false,
		    columns:[
				{title:"Down", field:"availability_down_date"},
				{title:"Up", field:"availability_recover_date"},
				{title:"Duration", field:"duration"},
				{title:"Type", field:"type"},
				{title:"Reason", field:"reason"},
		    ]
		});
		var res = parseAvailabilityData(eye);
		availabilityTable.setData(res.avails);
		//availabilityTable.redraw(true);		
		var eyeBtn = document.createElement("button");
		eyeBtn.classList.add("btn");
		
		if(res.currentlyDown){
			eyeBtn.classList.add("btn-danger");
		}else{
			eyeBtn.classList.add("btn-success");
		}
		eyeBtn.classList.add("btn-circle");
		eyeBtn.classList.add("btn-sm");
		
		const eyeStateNode = document.getElementById("eyeState")
		eyeStateNode.textContent = '';
		eyeStateNode.append(eyeBtn);
	}
}

function reload(){
	var secret = prompt("Enter admin secret");
	doRequestSecret("POST", "../reload", reloadDone,secret);
}

function reloadDone(){
	alert("Reload performed");
}

function parseAvailabilityData(eye){
	var res = {};
	var avails=[];
	var eyeIsCurrentlyDown = false;
	for (var i = 0; i < eye.availability.length; i++) {
		var down = eye.availability[i].availability_down_date == "null" ? "-" : eye.availability[i].availability_down_date;
		var recover = eye.availability[i].availability_recover_date == "null" ? "-" : eye.availability[i].availability_recover_date;
		var reason = eye.availability[i].availability_reason_description == "null"? "-":  eye.availability[i].availability_reason_description; 
		var duration;
		if(eye.availability[i].availability_recover_date != "null"){
			var ms = moment(eye.availability[i].availability_recover_date,"DD-MM-YYYY HH:mm:ss").diff(moment(eye.availability[i].availability_down_date,"DD-MM-YYYY HH:mm:ss"));
			duration = moment.utc(ms).format("HH:mm:ss");
		}else{
			duration = "Currently Down!"
			eyeIsCurrentlyDown=true;
		}
		
		var type = eye.availability[i].availability_reason_code == 1?"Connection failed":"Health NOK";
		var avail = {availability_down_date: down, availability_recover_date: recover, duration: duration, type: type, reason: reason};
		avails.push(avail);
	}
	res.avails = avails;
	res.currentlyDown = eyeIsCurrentlyDown;
	return res;
}

var paginationIndex=1;

function loadMoreAvailData(){
	paginationIndex=paginationIndex+1;
	var eyeID = getQueryParams(document.location.search).id;
	doRequest("GET", "../eye/"+eyeID+"/availability/"+paginationIndex, addToAvailHistory);
	
}

function addToAvailHistory(response){
	var res = parseAvailabilityData(response);
	availabilityTable.addData(res.avails);
	if(res.avails.length==0){
		document.getElementById("loadMoreAvailDataBtn").disabled=true;
	}
	window.scrollTo(0,document.body.scrollHeight);
}

function loadEyeOverview(response){
	const resNode = document.getElementById("res")
	resNode.textContent = '';
	  
	var ul = document.createElement("ul");
	ul.classList.add("list-group");

	for (let i = 0; i < response.length; i++) {

		var il = document.createElement("il");
		il.classList.add("list-group-item");

		const eye = response[i];
		var eyeDiv = document.createElement("div");

		var eyeBtn = document.createElement("button");
		eyeBtn.classList.add("btn");

		if(eye.availability_down_date != null && eye.availability_down_date != "null" && (eye.availability_recover_date == null || eye.availability_recover_date == "null")){
			eyeBtn.classList.add("btn-danger");
		}else{
			eyeBtn.classList.add("btn-success");
		}
		eyeBtn.classList.add("btn-circle");
		eyeBtn.classList.add("btn-md");
		eyeBtn.innerText = eye.version_string;
		eyeBtn.onclick = function() { window.location='index.html?page=eye&id='+eye.eye_id; };

		eyeDiv.appendChild(eyeBtn);

		var bold = document.createElement('strong');
		var infoP = document.createTextNode("  "+eye.eye_name);
		var infoV = document.createTextNode(" - "+eye.version_date);
		
		bold.appendChild(infoP);
		eyeDiv.appendChild(eyeBtn);
		eyeDiv.appendChild(bold);
		eyeDiv.appendChild(infoV);

		il.appendChild(eyeDiv);
		ul.appendChild(il);
	}
	resNode.appendChild(ul);
}