import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.util.Recurrence;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import javax.ejb.Stateless;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import static biweekly.util.Frequency.WEEKLY;
import static java.lang.Integer.valueOf;
import static java.time.LocalDateTime.of;
import static java.util.Arrays.asList;

@Stateless
public class Parser {

    private static final int columnWidth = 138;
    private static final int rowHeight = 72;
    private static final int offsetY = 124;

    private static int id = 0;

    static final String PATH = "/Users/stefan/IdeaProjects/ScheduleConverter/src/main/resources/results/file";

    int parsePDF(InputStream inputStream) {
        id++;
        int idTemp = id;
        String pathname = PATH + idTemp + ".ics";
        try {
            Biweekly.write(generateICal(parseInputStream(inputStream))).go(new File(pathname));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return idTemp;
    }

    private ICalendar generateICal(String[][] events) {
        // Begin of Semester
        LocalDate date = LocalDate.of(2018, 4, 9);
        ICalendar ical = new ICalendar();
        for (String[] event : events) {
            for (int j = 0; j < events.length; j++) {
                if (!event[j].trim().isEmpty()) {
                    ical.addEvent(generateEvent(extractEvent(event[j], date)));
                }
            }
            date = date.plusDays(1);
        }
        return ical;
    }

    private Event extractEvent(String s, LocalDate date) {
        Event event = new Event();
        String title = "";
        Scanner scanner = new Scanner(s);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            line = line.trim();
            if (line.matches("\\d+:\\d+-\\d+:\\d+")) {
                event.setTitle(title.trim());
                String[] split = line.split("-");
                for (int i = 0; i < split.length; i++) {
                    String[] time = split[i].split(":");
                    LocalDateTime dateTime = of(date, LocalTime.of(valueOf(time[0]), valueOf(time[1])));
                    if (i == 0) {
                        event.setStart(convertLocalDateTimeToDate(dateTime));
                    } else {
                        event.setEnd(convertLocalDateTimeToDate(dateTime));
                    }
                }
            } else if (event.getTitle() == null) {
                title += line + " ";
            } else {
                event.setLocation(line.trim());
                break;
            }
        }
        scanner.close();
        return event;
    }

    private Date convertLocalDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneOffset.systemDefault()).toInstant());
    }

    private String[][] parseInputStream(InputStream inputStream) {
        PDDocument document;
        String[][] events = new String[5][6];
        try {
            document = PDDocument.load(inputStream);

            if (!document.isEncrypted()) {
                PDFTextStripperByArea stripperByArea = new PDFTextStripperByArea();
                List<String> weekdays = asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
                List<String> slots = asList("08:00 - 10:00",
                                            "10:00 - 12:00",
                                            "12:00 - 14:00",
                                            "14:00 - 16:00",
                                            "16:00 - 18:00",
                                            "18:00 - 20:00");

                markRegions(events, stripperByArea, weekdays, slots);

                stripperByArea.extractRegions(document.getPage(0));

                extractEvents(events, stripperByArea, weekdays, slots);
            }
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return events;
    }

    private void extractEvents(String[][] events,
                               PDFTextStripperByArea stripperByArea,
                               List<String> weekdays,
                               List<String> slots) {
        for (int i = 0; i < events.length; i++) {
            for (int j = 0; j < events[i].length; j++) {
                events[i][j] = stripperByArea.getTextForRegion(weekdays.get(i) + " " + slots.get(j));
            }
        }
    }

    private void markRegions(String[][] events,
                             PDFTextStripperByArea stripperByArea,
                             List<String> weekdays,
                             List<String> slots) {
        int offsetX = 109;

        for (int i = 0; i < events.length; i++) {
            int y = offsetY;
            for (int j = 0; j < events[i].length; j++) {
                stripperByArea.addRegion(
                        weekdays.get(i) + " " + slots.get(j),
                        new Rectangle(offsetX, y, columnWidth, rowHeight)
                );
                y += rowHeight;
                if (i % 2 == 0) {
                    y--;
                }
            }
            offsetX += columnWidth;
        }
    }

    private VEvent generateEvent(Event event) {
        VEvent vEvent = new VEvent();

        vEvent.setSummary(event.getTitle());
        vEvent.setDateStart(event.getStart());
        vEvent.setDateEnd(event.getEnd());
        vEvent.setLocation(event.getLocation());

        // till end of Semester
        Recurrence recur = new Recurrence.Builder(WEEKLY)
                .interval(1)
                .until(convertLocalDateTimeToDate(of(2018, 7, 14, 23, 59)))
                .build();
        vEvent.setRecurrenceRule(recur);

        return vEvent;
    }
}