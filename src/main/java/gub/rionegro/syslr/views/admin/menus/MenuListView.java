package gub.rionegro.syslr.views.admin.menus;


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import gub.rionegro.syslr.data.BotMenu;
import gub.rionegro.syslr.data.BotMenuRepository;
import gub.rionegro.syslr.views.MainLayout;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Menús - ChatBot")
@Route(value = "admin/menus", layout = MainLayout.class)
@Menu(order = 2, title = "Menús", icon = LineAwesomeIconUrl.LIST_ALT_SOLID)
@RolesAllowed("ADMIN")
public class MenuListView extends VerticalLayout {

    private final BotMenuRepository menuRepository;
    private final Grid<BotMenu> grid;
    private final Binder<BotMenu> binder;
    private BotMenu currentMenu;

    public MenuListView(BotMenuRepository menuRepository) {
        this.menuRepository = menuRepository;
        this.binder = new Binder<>(BotMenu.class);
        this.grid = new Grid<>(BotMenu.class, false);

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
                new H2("Gestión de Menús"),
                new Button("Nuevo Menú",
                        new SvgIcon(LineAwesomeIconUrl.PLUS_SOLID),
                        e -> openForm(new BotMenu()))
        );
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        add(header);
    }

    private void createGrid() {
        grid.addColumn(BotMenu::getTitulo).setHeader("Título");
        grid.addColumn(BotMenu::getComando).setHeader("Comando");
        grid.addColumn(BotMenu::getDescripcion).setHeader("Descripción");

        grid.addComponentColumn(menu -> {
            Button editBtn = new Button("Editar",
                    new SvgIcon(LineAwesomeIconUrl.EDIT_SOLID));
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
            editBtn.addClickListener(e -> openForm(menu));

            Button deleteBtn = new Button("Eliminar",
                    new SvgIcon(LineAwesomeIconUrl.TRASH_SOLID));
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteBtn.addClickListener(e -> deleteMenu(menu));

            return new HorizontalLayout(editBtn, deleteBtn);
        }).setHeader("Acciones");

        grid.setWidthFull();
        grid.setHeight("70vh");

        add(grid);
    }

    private void createFormDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");

        FormLayout form = new FormLayout();

        TextField tituloField = new TextField("Título");
        TextField comandoField = new TextField("Comando");
        TextArea descripcionField = new TextArea("Descripción");

        binder.forField(tituloField)
                .asRequired("Título requerido")
                .bind(BotMenu::getTitulo, BotMenu::setTitulo);

        binder.forField(comandoField)
                .asRequired("Comando requerido")
                .bind(BotMenu::getComando, BotMenu::setComando);

        binder.forField(descripcionField)
                .bind(BotMenu::getDescripcion, BotMenu::setDescripcion);

        form.add(tituloField, comandoField, descripcionField);

        Button saveButton = new Button("Guardar", e -> saveMenu(dialog));
        Button cancelButton = new Button("Cancelar", e -> dialog.close());

        dialog.add(new VerticalLayout(
                new H2(currentMenu != null && currentMenu.getId() != null ?
                        "Editar Menú" : "Nuevo Menú"),
                form,
                new HorizontalLayout(saveButton, cancelButton)
        ));

        add(dialog);
    }

    private void openForm(BotMenu menu) {
        this.currentMenu = menu;
        binder.readBean(menu);

        getChildren()
                .filter(child -> child instanceof Dialog)
                .findFirst()
                .ifPresent(dialog -> ((Dialog) dialog).open());
    }

    private void saveMenu(Dialog dialog) {
        try {
            binder.writeBean(currentMenu);
            menuRepository.save(currentMenu);
            Notification.show("Menú guardado");
            dialog.close();
            loadData();
        } catch (Exception e) {
            Notification.show("Error: " + e.getMessage());
        }
    }

    private void deleteMenu(BotMenu menu) {
        menuRepository.delete(menu);
        Notification.show("Menú eliminado");
        loadData();
    }

    private void loadData() {
        grid.setItems(menuRepository.findAll());
    }
}