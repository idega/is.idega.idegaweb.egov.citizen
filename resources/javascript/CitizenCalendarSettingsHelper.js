CitizenCalendarSettingsHelper = {};
CitizenCalendarSettingsHelper._subscriptions = {};
CitizenCalendarSettingsHelper._subscriptions.add = [];
CitizenCalendarSettingsHelper._subscriptions.remove = [];

CitizenCalendarSettingsHelper.initialize = function(data){
	showLoadingMessage("");
	CitizenCalendarSettingsHelper.initializeSubscriptions(data);
	closeAllLoadingMessages();
}

CitizenCalendarSettingsHelper.initializeSubscriptions = function(data){
	var existing = data.subscribedCalendors;
	CitizenCalendarSettingsHelper._subscriptions.existing = existing.slice(0)
	CitizenCalendarSettingsHelper._subscriptions.next = existing.slice(0);
	jQuery("."+data.subscriptionsFieldsetClass).find("[type='checkbox']").each(function(){
		var checkBox = jQuery(this);
		var value = checkBox.val();
		var contains = false;
		for(var i = 0;i < existing.length;i++){
			if(value == existing[i]){
				contains = true;
				existing.splice(i,1);
				break;
			}
		}
		if(contains){
			checkBox.attr('checked', true);
		}else{
			checkBox.removeAttr('checked') 
		}
		checkBox.click(function(){
			CitizenCalendarSettingsHelper.subscribeCalendar(this);
		});
	});
}
CitizenCalendarSettingsHelper.subscribeCalendar = function(checkBox){
	var element = jQuery(checkBox);
	var uri = element.val();
	var checked = element.is(":checked");
	
	if(checked == true){
		CitizenCalendarSettingsHelper._subscribe(uri);
	}else{
		CitizenCalendarSettingsHelper._unSubscribe(uri);
	}
}

CitizenCalendarSettingsHelper._subscribe = function(uri){
	CitizenCalendarSettingsHelper._subscriptions.next.push(uri);
	
	var uris = CitizenCalendarSettingsHelper._subscriptions.remove;
	for(var i = 0;i < uris.length;i++){
		var theUri = uris[i];
		if(theUri == uri){
			uris.splice(i,1);
			break;
		}
	}
	
	uris = CitizenCalendarSettingsHelper._subscriptions.existing;
	var contains = false;
	for(var i = 0;i < uris.length;i++){
		var theUri = uris[i];
		if(theUri == uri){
			contains = true;
			break;
		}
	}
	
	if(contains){
		return;
	}
	
	uris = CitizenCalendarSettingsHelper._subscriptions.add;
	var contains = false;
	for(var i = 0;i < uris.length;i++){
		var theUri = uris[i];
		if(theUri == uri){
			contains = true;
			break;
		}
	}
	if(contains){
		return;
	}
	uris.push(uri);
}

CitizenCalendarSettingsHelper._unSubscribe = function(uri){
	var uris = CitizenCalendarSettingsHelper._subscriptions.next;
	for(var i = 0;i < uris.length;i++){
		var theUri = uris[i];
		if(theUri == uri){
			uris.splice(i,1);
			break;
		}
	}
	
	var uris = CitizenCalendarSettingsHelper._subscriptions.add;
	for(var i = 0;i < uris.length;i++){
		var theUri = uris[i];
		if(theUri == uri){
			uris.splice(i,1);
			break;
		}
	}
	
	uris = CitizenCalendarSettingsHelper._subscriptions.existing;
	var contains = false;
	for(var i = 0;i < uris.length;i++){
		var theUri = uris[i];
		if(theUri == uri){
			contains = true;
			break;
		}
	}
	
	if(!contains){
		return;
	}
	
	uris = CitizenCalendarSettingsHelper._subscriptions.remove;
	var contains = false;
	for(var i = 0;i < uris.length;i++){
		var theUri = uris[i];
		if(theUri == uri){
			contains = true;
			break;
		}
	}
	if(contains){
		return;
	}
	uris.push(uri);
}

CitizenCalendarSettingsHelper.save = function(){
//	alert("uris to add: " +  CitizenCalendarSettingsHelper._subscriptions.add + " uris to remove: " + CitizenCalendarSettingsHelper._subscriptions.remove + "uris next: " + CitizenCalendarSettingsHelper._subscriptions.next);
	var saveValues = {};
	saveValues.subscribedCalendars = CitizenCalendarSettingsHelper._subscriptions.add;
	saveValues.unsubscribedCalendars = CitizenCalendarSettingsHelper._subscriptions.remove;
	CitizenServices.saveCitizenCalendarSettings(saveValues,{
		callback : function(reply){
			if(reply.status != "OK"){
				// Actions for saving failure
				closeAllLoadingMessages();
				humanMsg.displayMsg(reply.message);
				return;
			}
			CitizenCalendarSettingsHelper._subscriptions.existing = CitizenCalendarSettingsHelper._subscriptions.next.slice(0);
			CitizenCalendarSettingsHelper._subscriptions.add = [];
			CitizenCalendarSettingsHelper._subscriptions.remove = [];
			closeAllLoadingMessages();
			humanMsg.displayMsg(reply.message);
			return;
		},
		errorHandler:function(message) {
			closeAllLoadingMessages();
			alert(message);
		}
	});
}
