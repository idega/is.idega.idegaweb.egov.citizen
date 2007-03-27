package is.idega.idegaweb.egov.citizen.business;

public class CitizenAccountSessionHomeImpl extends com.idega.business.IBOHomeImpl implements CitizenAccountSessionHome
{
 protected Class getBeanInterfaceClass(){
  return CitizenAccountSession.class;
 }


 public CitizenAccountSession create() throws javax.ejb.CreateException{
  return (CitizenAccountSession) super.createIBO();
 }

}