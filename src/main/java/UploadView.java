import org.primefaces.event.FileUploadEvent;

import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;

@Named
@SessionScoped
public class UploadView implements Serializable {

    @Inject
    private Parser parser;

    public void handleFileUpload(FileUploadEvent event) {
        int id;
        try {
            id = parser.parsePDF(event.getFile().getInputstream());
            if (id != 0) {
                FacesContext.getCurrentInstance()
                            .getExternalContext()
                            .redirect("/Stundenplan-1.0-SNAPSHOT/download.xhtml?faces-redirect=true&id=" + id);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
