package is.idega.idegaweb.egov.citizen.bean;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.idega.core.file.data.ICFile;
import com.idega.io.UploadFile;

@Component
@Scope("session")
public class SessionData {
	
	UploadFile uploadFile = null;

	int fileID;
	
	public UploadFile getUploadFile() {
		return uploadFile;
	}

	public void setUploadFile(UploadFile uploadFile) {
		this.uploadFile = uploadFile;
	}

	public int getFileID() {
		return fileID;
	}

	public void setFileID(int fileID) {
		this.fileID = fileID;
	}

	protected void finalize() throws Throwable {
		  super.finalize();
		  if (fileID > 0) deleteFile();
	}
	
	private void deleteFile() throws Exception {
		ICFile fileOld = ((com.idega.core.file.data.ICFileHome) com.idega.data.IDOLookup.getHome(ICFile.class)).findByPrimaryKey(new Integer(this.fileID));
		fileOld.delete();
	}
	
}
