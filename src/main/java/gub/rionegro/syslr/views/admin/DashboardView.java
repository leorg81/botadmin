package gub.rionegro.syslr.views.admin;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
        import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import gub.rionegro.syslr.views.MainLayout;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.lineawesome.LineAwesomeIcon;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Dashboard - ChatBot")
@Route(value = "admin/dashboard", layout = MainLayout.class)
@Menu(order = 1, title = "Dashboard", icon = LineAwesomeIconUrl.DASHCUBE)
@RolesAllowed({"ADMIN", "USER"})
public class DashboardView extends VerticalLayout {

    public DashboardView() {
        setSpacing(true);
        setPadding(true);
        setSizeFull();

        // Tarjetas de resumen
        add(createSummaryCards());

        // Gráficos
        add(createCharts());
    }

    private HorizontalLayout createSummaryCards() {
        HorizontalLayout cards = new HorizontalLayout();
        cards.setSpacing(true);
        cards.setWidthFull();

        cards.add(createCard("Total Clientes", "1,254", LineAwesomeIcon.USERS_SOLID, "#4CAF50"));
        cards.add(createCard("Sesiones Activas", "42", LineAwesomeIcon.COMMENTS_SOLID, "#2196F3"));
        cards.add(createCard("Flujos Configurados", "8", LineAwesomeIcon.FLUSHED_SOLID, "#9C27B0"));
        cards.add(createCard("Recibos Solicitados", "156", LineAwesomeIcon.FILE_INVOICE_SOLID, "#FF9800"));

        return cards;
    }

    private VerticalLayout createCard(String title, String value, LineAwesomeIcon icon, String color) {
        VerticalLayout card = new VerticalLayout();
        card.addClassNames("card", "padding");
        card.setSpacing(false);
        card.setPadding(true);
        card.getStyle()
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
                .set("background", "white")
                .set("min-width", "200px");

        HorizontalLayout header = new HorizontalLayout(
                new SvgIcon(icon.getSvgName()),
                new Span(title)
        );
        header.setAlignItems(Alignment.CENTER);
        header.setSpacing(true);

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-size", "24px")
                .set("font-weight", "bold")
                .set("color", color);

        card.add(header, valueSpan);
        return card;
    }

    private HorizontalLayout createCharts() {
        HorizontalLayout charts = new HorizontalLayout();
        charts.setSpacing(true);
        charts.setWidthFull();
        charts.setHeight("400px");

        charts.add(createSessionsChart());
        charts.add(createFlowsChart());

        return charts;
    }

    private Chart createSessionsChart() {
        Chart chart = new Chart(ChartType.COLUMN);
        chart.setWidth("50%");

        Configuration conf = chart.getConfiguration();
        conf.setTitle("Sesiones por Día");

        XAxis xAxis = new XAxis();
        xAxis.setCategories("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom");
        conf.addxAxis(xAxis);

        YAxis yAxis = new YAxis();
        yAxis.setTitle("Número de Sesiones");
        conf.addyAxis(yAxis);

        DataSeries series = new DataSeries();
        series.setName("Sesiones");
        series.setData(45, 52, 48, 61, 55, 32, 28);
        conf.addSeries(series);

        return chart;
    }

    private Chart createFlowsChart() {
        Chart chart = new Chart(ChartType.PIE);
        chart.setWidth("50%");

        Configuration conf = chart.getConfiguration();
        conf.setTitle("Uso de Flujos");

        DataSeries series = new DataSeries();
        series.add(new DataSeriesItem("Recibos", 45));
        series.add(new DataSeriesItem("Turnos", 30));
        series.add(new DataSeriesItem("Deudas", 15));
        series.add(new DataSeriesItem("Otros", 10));
        conf.addSeries(series);

        return chart;
    }
}