/**
 * 
 */
var UserAccessSettingsHelper = {};

jQuery(document).ready(function(){
//	UserAccessSettingsHelper.initialize();
});

UserAccessSettingsHelper.saveAccessSettings = function(selector){
	showLoadingMessage("");
	var form = jQuery(selector);
	var parameters = form.serializeArray();
	
	var map = {};
	for(var i = 0;i < parameters.length;i++){
		var element = parameters[i];
		if(map[element.name] == undefined){
			map[element.name] = [];
		}
		map[element.name].push(element.value);
	}
	humanMsg.displayMsg(map);
	CitizenServices.saveUserAccessSetings(map,{
		callback: function(reply){
			closeAllLoadingMessages();
			humanMsg.displayMsg(reply);
		}
	});
}
