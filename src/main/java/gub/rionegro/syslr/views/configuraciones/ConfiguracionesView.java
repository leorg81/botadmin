package gub.rionegro.syslr.views.configuraciones;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import gub.rionegro.syslr.services.UserService;
import gub.rionegro.syslr.views.MainLayout;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Configuraciones")
@Route(value = "configuraciones", layout = MainLayout.class)
@Menu(order = 6, icon = LineAwesomeIconUrl.COG_SOLID)
@RolesAllowed("ADMIN")
public class ConfiguracionesView extends Composite<VerticalLayout> {

    private final Div content = new Div();
    private final UserService userService;

    @Autowired
    public ConfiguracionesView(  UserService userService) {

        this.userService = userService;

        VerticalLayout layout = getContent();
        layout.setSizeFull();

        Tab tabUsuarios = new Tab("Usuarios");
        Tab tabTiposRecibo = new Tab("Tipos de Recibo");

        Tabs tabs = new Tabs(tabUsuarios, tabTiposRecibo);
        tabs.setWidthFull();
        tabs.setFlexGrowForEnclosedTabs(1);
        layout.add(new H2("Configuración del Sistema"), tabs, content);

        // Mostrar primera vista por defecto
        setView(createUsuariosView());

        tabs.addSelectedChangeListener(event -> {
            Tab selected = event.getSelectedTab();
            if (selected.equals(tabUsuarios)) {
                setView(createUsuariosView());
            }
        });
    }

    private void setView(com.vaadin.flow.component.Component component) {
        content.removeAll();
        content.add(component);
        content.setSizeFull();
    }



    private UsuariosView createUsuariosView() {
        return new UsuariosView(userService);
    }
}