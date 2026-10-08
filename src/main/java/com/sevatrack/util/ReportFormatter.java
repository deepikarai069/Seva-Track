package com.sevatrack.util;

import com.sevatrack.model.CategoryStat;
import com.sevatrack.model.DepartmentStat;
import com.sevatrack.service.ReportService.Report;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.List;
import java.util.Locale;

/** Department report as JSON (hand written, dependency free) or XML (JDK DOM). */
public final class ReportFormatter {
    private ReportFormatter() {}

    public static String toJson(Report r) {
        StringBuilder sb = new StringBuilder(1024);
        sb.append("{\n");
        sb.append("  \"generatedAt\": ").append(str(r.generatedAt().toString())).append(",\n");
        sb.append("  \"from\": ").append(str(r.from().toString())).append(",\n");
        sb.append("  \"to\": ").append(str(r.to().toString())).append(",\n");
        sb.append("  \"departments\": [");
        List<DepartmentStat> ds = r.departments();
        for (int i = 0; i < ds.size(); i++) {
            DepartmentStat d = ds.get(i);
            sb.append(i == 0 ? "\n" : ",\n");
            sb.append("    {\"id\": ").append(d.getDepartmentId())
              .append(", \"name\": ").append(str(d.getDepartmentName()))
              .append(", \"total\": ").append(d.getTotal())
              .append(", \"open\": ").append(d.getOpen())
              .append(", \"resolved\": ").append(d.getResolved())
              .append(", \"escalated\": ").append(d.getEscalated())
              .append(", \"slaBreached\": ").append(d.getBreached())
              .append(", \"avgResolutionHours\": ").append(String.format(Locale.ROOT, "%.2f", d.getAvgResolutionHours()))
              .append(", \"categories\": [");
            boolean first = true;
            for (CategoryStat c : r.categories()) {
                if (c.getDepartmentId() != d.getDepartmentId()) continue;
                if (!first) sb.append(", ");
                first = false;
                sb.append("{\"name\": ").append(str(c.getCategoryName()))
                  .append(", \"total\": ").append(c.getTotal())
                  .append(", \"open\": ").append(c.getOpen()).append('}');
            }
            sb.append("]}");
        }
        sb.append(ds.isEmpty() ? "]\n" : "\n  ]\n").append("}\n");
        return sb.toString();
    }

    public static String toXml(Report r) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Document doc = f.newDocumentBuilder().newDocument();
            Element root = doc.createElement("report");
            root.setAttribute("generatedAt", r.generatedAt().toString());
            root.setAttribute("from", r.from().toString());
            root.setAttribute("to", r.to().toString());
            doc.appendChild(root);
            for (DepartmentStat d : r.departments()) {
                Element e = doc.createElement("department");
                e.setAttribute("id", String.valueOf(d.getDepartmentId()));
                e.setAttribute("name", d.getDepartmentName());
                e.setAttribute("total", String.valueOf(d.getTotal()));
                e.setAttribute("open", String.valueOf(d.getOpen()));
                e.setAttribute("resolved", String.valueOf(d.getResolved()));
                e.setAttribute("escalated", String.valueOf(d.getEscalated()));
                e.setAttribute("slaBreached", String.valueOf(d.getBreached()));
                e.setAttribute("avgResolutionHours", String.format(Locale.ROOT, "%.2f", d.getAvgResolutionHours()));
                for (CategoryStat c : r.categories()) {
                    if (c.getDepartmentId() != d.getDepartmentId()) continue;
                    Element ce = doc.createElement("category");
                    ce.setAttribute("name", c.getCategoryName());
                    ce.setAttribute("total", String.valueOf(c.getTotal()));
                    ce.setAttribute("open", String.valueOf(c.getOpen()));
                    e.appendChild(ce);
                }
                root.appendChild(e);
            }
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            StringWriter w = new StringWriter();
            t.transform(new DOMSource(doc), new StreamResult(w));
            return w.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Could not build XML report", e);
        }
    }

    static String str(String s) {
        if (s == null) return "null";
        StringBuilder sb = new StringBuilder(s.length() + 2).append('"');
        for (char ch : s.toCharArray()) {
            switch (ch) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (ch < 0x20) sb.append(String.format("\\u%04x", (int) ch));
                    else sb.append(ch);
                }
            }
        }
        return sb.append('"').toString();
    }
}
