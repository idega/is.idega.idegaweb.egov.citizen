var SimpleUserEditFormHelper = {};
jQuery(document).ready(function(){
	SimpleUserEditFormHelper.initialize();
	SimpleUserEditFormHelper.createUserAutocomplete(SimpleUserEditFormHelper.RELATION_INPUT_SELECTOR,
			SimpleUserEditFormHelper.RELATION_SELECT_SELECTOR,
			SimpleUserEditFormHelper.RELATION_LIST_SELECTOR);
	SimpleUserEditFormHelper.addRemoveActions(SimpleUserEditFormHelper.REMOVE_ITEM_CLASS,
			SimpleUserEditFormHelper.RELATED_ITEM_CLASS,
			SimpleUserEditFormHelper.ID_CONTANER_CLASS,
			SimpleUserEditFormHelper.RELATION_TYPE_CONTAINER_CLASS,
			SimpleUserEditFormHelper.FORM_SELECTOR,
			SimpleUserEditFormHelper.USER_EDIT_USER_ID_PARAMETER);
	SimpleUserEditFormHelper.createAutoresizing(SimpleUserEditFormHelper.RESUME_INPUT_SELECTOR,
			SimpleUserEditFormHelper.FORM_SELECTOR);
	SimpleUserEditFormHelper.addDatePicker(SimpleUserEditFormHelper.FORM_SELECTOR,
			SimpleUserEditFormHelper.USER_EDIT_DATE_SELECTOR);
	jQuery(".tagedit-list").css({
		width : "100%",
		padding : "0"
	});
});

SimpleUserEditFormHelper.createAutoresizing = function(Areaselector,formSelector){
	var textArea = jQuery(Areaselector);
	textArea.width("auto");
	textArea.attr("cols",50);
	var areaWidth = textArea.width();
	var formWidth = jQuery(formSelector).width();
	var columns = formWidth / areaWidth;
	textArea.attr("cols",columns * 50);
	var areaWidth = textArea.width() + 30;
	columns = textArea.attr("cols");
	if(formWidth > areaWidth){
		for(var cols = textArea.attr("cols");formWidth > areaWidth;cols++){
			textArea.attr("cols",cols);
			areaWidth = textArea.width() + 30;
		}
	}
	for(var cols = textArea.attr("cols");formWidth < areaWidth;cols--){
		textArea.attr("cols",cols);
		areaWidth = textArea.width() + 30;
	}
	textArea.autoResize({extraSpace : 20, animate : false,limit: 9999 });
	textArea.trigger("keyup");
}

SimpleUserEditFormHelper.saveUser = function(selector){
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
	CitizenServices.saveUser(map, {
		callback: function(reply){
			closeAllLoadingMessages();
			humanMsg.displayMsg(reply);
		}
	});
	
}
SimpleUserEditFormHelper.addRemoveActions = function(removeItemClass,relatedItemClass,idContainerClass,relationTypeContainerClass,formSelector,userIdInputName){
	jQuery("." + removeItemClass).each(function(){
		var removeItem = jQuery(this);
		removeItem.click(function(){
			var li = removeItem.parents("li." + relatedItemClass).filter(":first");
		
			var relatedId = li.find("." + idContainerClass).val();
			var relationType = li.find("." + relationTypeContainerClass).val();
			var userId = jQuery(formSelector).find("[name='"+ userIdInputName +"']").val();
			
			CitizenServices.removeRelation(userId,relatedId,relationType, {
				callback: function(isRemoved) {
					if(isRemoved){
						li.remove();
						humanMsg.displayMsg(SimpleUserEditFormHelper.REMOVED_MSG);
					}else{
						humanMsg.displayMsg(SimpleUserEditFormHelper.FAILED_MSG);
					}
				}
			});
		});
		
	});
}

SimpleUserEditFormHelper.createUserAutocomplete = function(inputSelector,relationSelectSelector,relationList){
	input = jQuery(inputSelector);
	input.tagedit({
		autocompleteURL: function(request, response) {
			var relationName = jQuery(relationSelectSelector).val();
			CitizenServices.getAutocompletedUsers(request.term,5,relationName,  {
				callback: function(serverResponse) {
					if(serverResponse.status[0] != "OK"){
						humanMsg.displayMsg(serverResponse.message[0]);
						return;
					}
					var userDataCollection = serverResponse.content;
					var arrayOfData = [];
					var i = 0;
					var end = userDataCollection.length - 1;
					while(i < end){
						var item = {
								label : userDataCollection[i],
								value : userDataCollection[i+1]
						};
						arrayOfData.push(item);
						i += 2;
					}
					response(arrayOfData);
				}
			});
		},
		allowEdit: true,
		allowAdd: true,
		delay: 100,
		autocompleteOptions: {
			minLength : 3,
			html: true,
			select: function( event, ui ) {
				jQuery(inputSelector).val(ui.item.value).trigger('transformToTag', [ui.item.id, ui.item.label]);
				return false;
			}
		},
		transform : function(event, id, label) {
			var obj = jQuery(inputSelector).data("tag-options-data");
			var oldValue = (typeof id != 'undefined' && id.length > 0);

			if(label == undefined){
				var request  = jQuery(inputSelector).val();
				var relationName = jQuery(relationSelectSelector).val();
				CitizenServices.getAutocompletedUsers(request,2,relationName, {
					callback: function(response) {
						if(response.status[0] != "OK"){
							humanMsg.displayMsg(response.message[0]);
							return;
						}
						var userDataCollection = response.content;
						if(userDataCollection.length == 2){
							jQuery(inputSelector).trigger('transformToTag', [undefined, userDataCollection[0]]);
						}
						else{
							humanMsg.displayMsg(SimpleUserEditFormHelper.NON_TRIVIAL_USER_PHRASE);
							jQuery(inputSelector).focus();
						}
					}
				});
				return false;
			}
			var checkAutocomplete = oldValue == true? false : true;
			// check if the Value ist new
			var isNewResult = obj.isNew(label.toString(), checkAutocomplete);
			if(isNewResult[0] === true || (isNewResult[0] === false && typeof isNewResult[1] == 'string')) {

				if(oldValue == false && typeof isNewResult[1] == 'string') {
					oldValue = true;
					id = isNewResult[1];
				}

				if(obj.options.allowAdd == true || oldValue) {
					// Make a new tag in front the input
					html = '<li class="tagedit-listelement tagedit-listelement-old">';
					html += '<span dir="'+obj.options.direction+'">' + label + '</span>';
					html += "<input type='hidden' name = 'tag[]' disabled='disabled'" + " value=\"" + label.toString() +"\" />";
					html += '<a class="tagedit-close" title="'+obj.options.texts.removeLinkTitle+'">x</a>';
					html += '</li>';
					var element = jQuery('<li/>');
					element.append(jQuery(html).find("table"));
					jQuery(relationList).append(element);
					var removeItem = element.find("." + SimpleUserEditFormHelper.REMOVE_ITEM_CLASS);
					removeItem.removeClass(SimpleUserEditFormHelper.REMOVE_ITEM_CLASS);
					removeItem.click(function(){
						element.remove();
					});
				}
			}
			jQuery(this).val('');

			// close autocomplete
			if(obj.options.autocompleteOptions.source) {
				jQuery(this).autocomplete( "close" );
			}

		}
	});
	input.inputsToList = function(){};
}
SimpleUserEditFormHelper.addDatePicker = function(formSelector,dateSelector){
	nana = jQuery(formSelector);
	nana = jQuery(formSelector).find(dateSelector);
	jQuery(formSelector).find(dateSelector).datepicker({
		changeMonth: true,
		changeYear: true
	});
}

SimpleUserEditFormHelper.showUserInput = function(input,containerId){
	var value = jQuery(input).val();
	if(value == "-1"){
		jQuery("#"+ containerId).css("visibility","hidden");
		return;
	}
	jQuery("#"+ containerId).css("visibility","visible");
}

