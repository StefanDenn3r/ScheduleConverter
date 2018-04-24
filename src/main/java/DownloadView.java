import org.apache.pdfbox.io.IOUtils;

import javax.annotation.PostConstruct;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.io.*;
import java.util.Map;

import static java.lang.Integer.valueOf;

@Named
@SessionScoped
public class DownloadView implements Serializable {

    private int id;

    @PostConstruct
    public void download() {
        Map<String, String> parameterMap = FacesContext.getCurrentInstance()
                                                       .getExternalContext()
                                                       .getRequestParameterMap();
        id = valueOf(parameterMap.get("id"));
        String pathname = Parser.PATH + id + ".ics";

        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();

        ec.responseReset();
        ec.setResponseContentType("application/pdf");
        ec.setResponseHeader("Content-Disposition", "attachment; filename=\"" + "Schedule.ics" + "\"");

        try {
            OutputStream out = ec.getResponseOutputStream();
            InputStream in;
            in = new FileInputStream(pathname);
            IOUtils.copy(in, out);
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        fc.responseComplete();
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
