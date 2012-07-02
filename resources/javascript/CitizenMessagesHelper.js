var CitizenMessagesHelper = {};

CitizenMessagesHelper._data = {};
CitizenMessagesHelper.addRow = function(element,fieldsetStyleClass,containerStyleClass,formHtmlStyleClass){
	var element = jQuery(element);
	var container = element.parents().filter("." + fieldsetStyleClass).find("." + containerStyleClass);
	var form = element.parent().find("."+formHtmlStyleClass).children();
	var newForm = form.clone();
	container.append(newForm);
	
	// Labelify
	var inputs = newForm.find("[type='text']").labelify({labelledClass: "labelinside"});
}

CitizenMessagesHelper.deleteEmail = function(element){
	var row = jQuery(element).parents().filter(".data-row");
	var id = row.find("[name='id']").val();
	row.remove();
	if(id == "-1"){
		return;
	}
	var emailsToRemove = CitizenMessagesHelper._data.emailsToRemove;
	if((emailsToRemove == undefined) || (emailsToRemove == null)){
		emailsToRemove = [];
	}
	emailsToRemove.push(id);
	CitizenMessagesHelper._data.emailsToRemove = emailsToRemove;
}

CitizenMessagesHelper.deletePhone = function(element){
	var row = jQuery(element).parents().filter(".data-row");
	var id = row.find("[name='id']").val();
	row.remove();
	if(id == "-1"){
		return;
	}
	var phonesToRemove = CitizenMessagesHelper._data.phonesToRemove;
	if((phonesToRemove == undefined) || (phonesToRemove == null)){
		phonesToRemove = [];
	}
	phonesToRemove.push(id);
	CitizenMessagesHelper._data.phonesToRemove = phonesToRemove;
}


CitizenMessagesHelper.getEmails = function(emailFieldset){
	var emails = [];
	var errors = false;
	var emailRows = emailFieldset.find(".data-row");
	for(var i = 0;i < emailRows.length;i++){
		var row = jQuery(emailRows[i]);
		var rowNumber = row.find(".row-number");
		if(rowNumber.length > 0){
			rowNumber.val(i);
		}else{
			var rowNumberContainer = jQuery("<intput type='hidden' value='"+ i +"' class='row-number' />")
			row.append(rowNumberContainer);
		}
		var email = {};
		email.rowNumber = i;
		email.value = row.find("[name='value']").val();
		email.purpose = row.find("[name='contact-purpose']").val();
		email.id = row.find("[name='id']").val();
		var errorMessage = row.find(".error-message");
		errorMessage.remove();
		var error = CitizenMessagesHelper._checkEmail(email);
		if(error == null){
			emails.push(email);
		}else{
			errorMessage = CitizenMessagesHelper.createErrorMessage(error);
			row.append(errorMessage);
			errors = true;
		}
	}
	var data = {};
	data.emails = emails;
	data.errors = errors;
	return data;
}

CitizenMessagesHelper.getPhones = function(phoneFieldset){
	var phones = [];
	var errors = false;
	var phoneRows = phoneFieldset.find(".data-row");
	for(var i = 0;i < phoneRows.length;i++){
		var row = jQuery(phoneRows[i]);
		var rowNumber = row.find(".row-number");
		if(rowNumber.length > 0){
			rowNumber.val(i);
		}else{
			var rowNumberContainer = jQuery("<intput type='hidden' value='"+ i +"' class='row-number' />")
			row.append(rowNumberContainer);
		}
		var phone = {};
		phone.rowNumber = i;
		phone.value = row.find("[name='value']").val();
		phone.purpose = row.find("[name='contact-purpose']").val();
		phone.phoneTypeId = row.find("[name='phone-type']").val();
		phone.id = row.find("[name='id']").val();
		var errorMessage = row.find(".error-message");
		errorMessage.remove();
		var error = CitizenMessagesHelper._checkPhone(phone);
		if(error == null){
			phones.push(phone);
		}else{
			errorMessage = CitizenMessagesHelper.createErrorMessage(error);
			row.append(errorMessage);
			errors = true;
		}
	}
	var data = {};
	data.phones = phones;
	data.errors = errors;
	return data;
}

CitizenMessagesHelper.save = function(element,formClass,emailsFieldsetClass,phoneListClass){
	showLoadingMessage("");
	var form = jQuery(element).parents().filter("."+formClass);
	var saveValues = {};
	var emailFieldset = form.find("."+emailsFieldsetClass);
	var emailData = CitizenMessagesHelper.getEmails(emailFieldset);
	saveValues.emails = emailData.emails;
	
	var phoneFieldset = form.find("."+phoneListClass);
	
	var phoneData = CitizenMessagesHelper.getPhones(phoneFieldset);
	saveValues.phones = phoneData.phones;
	
	if(emailData.errors || phoneData.errors){
		closeAllLoadingMessages();
		humanMsg.displayMsg(CitizenMessagesLocalized.SAVING_FAILED);
		return;
	}
	
	var dataToRemove = {};
	dataToRemove.emailsToRemove = CitizenMessagesHelper._data.emailsToRemove;
	dataToRemove.phonesToRemove = CitizenMessagesHelper._data.phonesToRemove;
	
	CitizenServices.saveUserMessagesData(saveValues,dataToRemove,{
		callback : function(reply){
			if(reply.status != "OK"){
				// Actions for saving failure
				closeAllLoadingMessages();
				humanMsg.displayMsg(reply.message);
				return;
			}
			CitizenMessagesHelper._data.emailsToRemove = [];
			CitizenMessagesHelper._data.phonesToRemove = [];
			CitizenMessagesHelper._updateRows(emailFieldset,reply.emails,true);
			CitizenMessagesHelper._updateRows(phoneFieldset,reply.phones);
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

CitizenMessagesHelper.createErrorMessage = function(message){
	var errorMessage = jQuery("<div class='error-message'/>");
	errorMessage.css({
		"position" : "relative"
	});
	var span = jQuery("<span class='txt'>"+ message +"</span>");
	errorMessage.append(span);
	errorMessage.hover(function(){
		var element = jQuery(this);
		element.find(".txt").css({
			"opacity" : "0.5"
		});
		element.find(".close").show();
	});
	errorMessage.mouseleave(function(){
		var element = jQuery(this);
		element.find(".txt").css({
			"opacity" : "1"
		});
		element.find(".close").hide();
	});
	var close = jQuery("<div class='close'>x</div>");
	errorMessage.append(close);
	close.css({
		"color" : "#FFFFFF",
		"display" : "none",
		"background" : "#99AABB",
		"opacity"	:	"0.85",
		"border-radius" : "45%",
		"text-align"	: "center",
		"width"			: "1.5em",
		"height"		: "1.5em",
		"cursor"		: "pointer",
		"position"	: "absolute",
		"right"		: "0",
		"top"		: "0"
	});
	close.click(function(){
		jQuery(this).parent().remove();
	});
	
	return errorMessage;
}
CitizenMessagesHelper._updateRows = function(container,dataList,changeValue){
	for(var i = 0; i < dataList.length;i++){
		var data = dataList[i];
		row = container.find(".row-number").filter("[value='"+data.rowNumber+"']").parent();
		if(data.id == "-1"){
			row.remove();
			continue;
		}
		row.find("[name='id']").val(data.id);
		if(changeValue == true){
			row.find("[name='value']").val(data.value);
		}
	}
}

CitizenMessagesHelper._checkEmail = function(mail){
	var reg = /^([A-Za-z0-9_\-\.])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,4})$/;
	if(reg.test(mail.value) == false) {
		return CitizenMessagesLocalized.INCORRECT_MAIL;
	}
	if((mail.purpose == undefined) || (mail.purpose == null) || (mail.purpose == "-1")){
		return CitizenMessagesLocalized.PURPOSE_NOT_SPECIFIED;
	}
	return null;
}

CitizenMessagesHelper._checkPhone = function(phone){
	var isNonblank_re    = /\S/;
	var val = phone.value;
	if((val == undefined) || (val == null) || (val == "-1") || (val.search (isNonblank_re) == -1)){
		return CitizenMessagesLocalized.CAN_NOT_BE_EMPTY;
	}
	if((phone.phoneTypeId == undefined) || (phone.phoneTypeId == null) || (phone.phoneTypeId == "-1")){
		return CitizenMessagesLocalized.PHONE_TYPE_NOT_SPECIFIED;
	}
	if((phone.purpose == undefined) || (phone.purpose == null) || (phone.purpose == "-1")){
		return CitizenMessagesLocalized.PURPOSE_NOT_SPECIFIED;
	}
	return null;
}
