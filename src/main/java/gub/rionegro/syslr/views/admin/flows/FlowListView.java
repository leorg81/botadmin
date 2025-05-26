package gub.rionegro.syslr.views.admin.flows;


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import gub.rionegro.syslr.data.BotFlow;
import gub.rionegro.syslr.data.BotFlowRepo;
import gub.rionegro.syslr.data.BotMenu;
import gub.rionegro.syslr.data.BotMenuRepository;
import gub.rionegro.syslr.views.MainLayout;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.lineawesome.LineAwesomeIcon;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Flujos - ChatBot")
@Route(value = "admin/flows", layout = MainLayout.class)
@Menu(order = 3, title = "Flujos", icon = LineAwesomeIconUrl.FLUSHED_SOLID)
@RolesAllowed("ADMIN")
public class FlowListView extends VerticalLayout {

    private final BotFlowRepo flowRepo;
    private final BotMenuRepository menuRepo;
    private final Grid<BotFlow> grid;
    private final Binder<BotFlow> binder;

    public FlowListView(BotFlowRepo flowRepo, BotMenuRepository menuRepo) {
        this.flowRepo = flowRepo;
        this.menuRepo = menuRepo;
        this.binder = new Binder<>(BotFlow.class);
        this.grid = new Grid<>(BotFlow.class, false);

        setSpacing(true);
        setPadding(true);
        setSizeFull();

        createHeader();
        createGrid();
        createFormDialog();

        loadData();
    }

    private void createHeader() {
        HorizontalLayout header = new HorizontalLayout(
                new H2("Gestión de Flujos"),
                new Button("Nuevo Flujo",
                        new SvgIcon(LineAwesomeIconUrl.PLUS_SOLID),
                        e -> openForm(new BotFlow()))
        );
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        add(header);
    }

    private void createGrid() {
        grid.addColumn(BotFlow::getCode).setHeader("Código");
        grid.addColumn(BotFlow::getName).setHeader("Nombre");
        grid.addColumn(flow ->
                flow.getMenu() != null ? flow.getMenu().getTitulo() : "Sin menú"
        ).setHeader("Menú");

        grid.addComponentColumn(flow ->
                new Button("Pasos",
                        new SvgIcon(LineAwesomeIconUrl.LIST_SOLID),
                        e -> getUI().ifPresent(ui ->
                                ui.navigate("admin/steps?flowId=" + flow.getId())
                        ))
        ).setHeader("Acciones");

        grid.setWidthFull();
        add(grid);
    }

    private void createFormDialog() {
        Dialog dialog = new Dialog();
        FormLayout form = new FormLayout();

        TextField codeField = new TextField("Código");
        TextField nameField = new TextField("Nombre");
        ComboBox<BotMenu> menuCombo = new ComboBox<>("Menú");
        menuCombo.setItems(menuRepo.findAll());
        menuCombo.setItemLabelGenerator(BotMenu::getTitulo);

        binder.forField(codeField).bind(BotFlow::getCode, BotFlow::setCode);
        binder.forField(nameField).bind(BotFlow::getName, BotFlow::setName);
        binder.forField(menuCombo).bind(BotFlow::getMenu, BotFlow::setMenu);

        form.add(codeField, nameField, menuCombo);

        Button saveButton = new Button("Guardar", e -> {
            try {
                BotFlow flow = new BotFlow();
                binder.writeBean(flow);
                flowRepo.save(flow);
                Notification.show("Flujo guardado");
                dialog.close();
                loadData();
            } catch (Exception ex) {
                Notification.show("Error: " + ex.getMessage());
            }
        });

        dialog.add(new VerticalLayout(new H2("Nuevo Flujo"), form, saveButton));
        add(dialog);
    }

    private void openForm(BotFlow flow) {
        binder.readBean(flow);
        getChildren()
                .filter(child -> child instanceof Dialog)
                .findFirst()
                .ifPresent(dialog -> ((Dialog) dialog).open());
    }

    private void loadData() {
        grid.setItems(flowRepo.findAll());
    }
}