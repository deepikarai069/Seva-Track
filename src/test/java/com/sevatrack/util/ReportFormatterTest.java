package com.sevatrack.util;

import com.sevatrack.model.CategoryStat;
import com.sevatrack.model.DepartmentStat;
import com.sevatrack.service.ReportService.Report;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static com.sevatrack.TestData.NOW;
import static org.junit.jupiter.api.Assertions.*;

class ReportFormatterTest {
    private Report report() {
        DepartmentStat d = new DepartmentStat();
        d.setDepartmentId(2);
        d.setDepartmentName("Roads & \"Infra\" <Dept>");
        d.setTotal(10); d.setOpen(4); d.setResolved(6); d.setEscalated(3); d.setBreached(1); d.setAvgResolutionHours(12.5);
        CategoryStat c = new CategoryStat();
        c.setDepartmentId(2); c.setCategoryName("Pothole"); c.setTotal(7); c.setOpen(2);
        CategoryStat other = new CategoryStat();
        other.setDepartmentId(3); other.setCategoryName("Garbage"); other.setTotal(1); other.setOpen(1);
        return new Report(LocalDate.of(2026, 9, 8), LocalDate.of(2026, 10, 7), NOW, List.of(d), List.of(c, other));
    }

    @Test
    void jsonEscapesSpecialCharactersAndNestsOnlyOwnCategories() {
        String json = ReportFormatter.toJson(report());
        assertTrue(json.contains("\"name\": \"Roads & \\\"Infra\\\" <Dept>\""), json);
        assertTrue(json.contains("\"avgResolutionHours\": 12.50"));
        assertTrue(json.contains("\"Pothole\""));
        assertFalse(json.contains("Garbage"));
        assertEquals(json.chars().filter(ch -> ch == '{').count(), json.chars().filter(ch -> ch == '}').count());
    }

    @Test
    void jsonStringEscapingHandlesControlCharacters() {
        assertEquals("\"a\\nb\\t\\\\\\u0001\"", ReportFormatter.str("a\nb\t\\\u0001"));
        assertEquals("null", ReportFormatter.str(null));
    }

    @Test
    void xmlIsWellFormedAndRoundTripsValues() throws Exception {
        String xml = ReportFormatter.toXml(report());
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        NodeList depts = doc.getElementsByTagName("department");
        assertEquals(1, depts.getLength());
        Element d = (Element) depts.item(0);
        assertEquals("Roads & \"Infra\" <Dept>", d.getAttribute("name"));
        assertEquals("1", d.getAttribute("slaBreached"));
        assertEquals(1, d.getElementsByTagName("category").getLength());
    }

    @Test
    void emptyReportStillProducesValidOutput() throws Exception {
        Report empty = new Report(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 7), NOW, List.of(), List.of());
        assertTrue(ReportFormatter.toJson(empty).contains("\"departments\": []"));
        DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new ByteArrayInputStream(ReportFormatter.toXml(empty).getBytes(StandardCharsets.UTF_8)));
    }
}
