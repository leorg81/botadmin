package gub.rionegro.syslr.views.configuraciones;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import com.vaadin.flow.data.provider.ListDataProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import gub.rionegro.syslr.data.Role;
import gub.rionegro.syslr.data.User;
import gub.rionegro.syslr.services.UserService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class UsuariosView extends VerticalLayout {

    private final UserService userService;

    private final Grid<User> userGrid = new Grid<>(User.class);
    private final ListDataProvider<User> dataProvider;

    @Autowired
    public UsuariosView(UserService userService) {
        this.userService = userService;
        this.dataProvider = new ListDataProvider<>(userService.findAll());

        setUpGrid();
        add(createHeader(), userGrid);
    }

    private HorizontalLayout createHeader() {
        Button addButton = new Button("Agregar Usuario", e -> openUserDialog(null));
        return new HorizontalLayout(addButton);
    }

    private void setUpGrid() {
        userGrid.setDataProvider(dataProvider);
        userGrid.setColumns("username", "name","correo","roles");
        userGrid.addComponentColumn(user -> createImage(user.getProfilePicture()))
                .setHeader("Foto");
        userGrid.addComponentColumn(user -> createActions(user))
                .setHeader("Acciones");
    }

    private HorizontalLayout createActions(User user) {
        Button editButton = new Button("Editar", e -> openUserDialog(user));
        Button deleteButton = new Button("Eliminar", e -> {
            userService.delete(user.getId());
            dataProvider.getItems().remove(user);
            dataProvider.refreshAll();
            Notification.show("Usuario eliminado.");
        });
        return new HorizontalLayout(editButton, deleteButton);
    }


    private void openUserDialog(User user) {
        Dialog dialog = new Dialog();

        // Crear los campos de entrada
        TextField usernameField = new TextField("Usuario");
        PasswordField passwordField = new PasswordField("Nueva Contraseña");
        PasswordField newPasswordField = new PasswordField("Confirmar nueva contraseña");
        passwordField.setVisible(false);
        newPasswordField.setVisible(false);

        Button changePasswordButton = new Button("Cambiar Contraseña");
        changePasswordButton.addClickListener(e -> {
            passwordField.setEnabled(true);
            newPasswordField.setVisible(true);
            passwordField.setVisible(true);
            newPasswordField.setEnabled(true);
            changePasswordButton.setVisible(false);
        });



        TextField nombreField = new TextField("Nombre");
        TextField correoField = new TextField("Correo");


        // Crear ComboBoxes para rol, localidad y sector
        MultiSelectComboBox<Role> rolComboBox = new MultiSelectComboBox<Role>("Roles");


        // Crear un FileBuffer para cargar la foto
        FileBuffer fileBuffer = new FileBuffer();
        Upload upload = new Upload(fileBuffer);
        ByteArrayOutputStream imageBuffer = new ByteArrayOutputStream();
        Image imagePreview = new Image();
        imagePreview.setWidth("50px"); // Ajustar el tamaño de la imagen de vista previa
        imagePreview.setHeight("auto");

        // Establecemos los tipos de archivo aceptados para la foto
        upload.setAcceptedFileTypes("image/jpeg", "image/png");
        upload.addSucceededListener(event -> {
            try {
                InputStream fileInputStream = fileBuffer.getInputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    imageBuffer.write(buffer, 0, bytesRead);
                }
                // Mostrar la imagen cargada en la vista previa
                String base64Image = "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(imageBuffer.toByteArray());
                imagePreview.setSrc(base64Image);
            } catch (IOException e) {
                Notification.show("Error al cargar la imagen.");
            }
        });

        // Si el usuario es null, inicializamos uno nuevo
        if (user == null) {
            user = new User();
        }

        // Prellenar los campos con los datos del usuario (si no son null)
        if (user.getProfilePicture() != null && user.getProfilePicture().length > 0) {
            String base64Image = "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(user.getProfilePicture());
            imagePreview.setSrc(base64Image);
        }
        usernameField.setValue(user.getUsername() != null ? user.getUsername() : "");
        nombreField.setValue(user.getName() != null ? user.getName() : "");
        correoField.setValue(user.getCorreo() != null ? user.getCorreo() :"");

        // Prellenar los ComboBoxes con los valores del rol, localidad y sector
        rolComboBox.setItems(Role.values()); // Asegúrate de que Rol es un enum
        rolComboBox.setValue(user.getRoles());

        // Aquí deberías cargar los datos de Localidad y Sector de tu base de datos



        // Botón para guardar los datos del usuario
        User finalUser = user;
        Button saveButton = new Button("Guardar", e -> {

            if (usernameField.getValue().isEmpty()) {
                Notification.show("El nombre de usuario no puede estar vacío.");
                return;
            }

            // Actualizar los datos del usuario
            finalUser.setUsername(usernameField.getValue());
            finalUser.setName(nombreField.getValue());
            finalUser.setCorreo(correoField.getValue());
            finalUser.setRoles(rolComboBox.getValue());

            // Si se cargó una imagen, la asignamos al usuario
            if (imageBuffer.size() > 0) {
                finalUser.setProfilePicture(imageBuffer.toByteArray());
            }

            // Validación de contraseña

            if (passwordField.isEnabled() && !passwordField.getValue().isEmpty() && passwordField.isVisible()) {
                if (passwordField.getValue().equals(newPasswordField.getValue())) {
                    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                    finalUser.setHashedPassword(passwordEncoder.encode(passwordField.getValue()));
                } else {
                    Notification.show("Las contraseñas no coinciden.", 3000, Notification.Position.MIDDLE);
                    return;
                }
            }

            // Guardar los datos en la base de datos
            userService.update(finalUser);

            // Actualizar la vista con los cambios
            dataProvider.getItems().clear();
            dataProvider.getItems().addAll(userService.findAll());
            dataProvider.refreshAll();
            dialog.addDialogCloseActionListener(event -> this.remove(dialog));
            Notification.show("Usuario guardado.");
            dialog.close();
        });

        // Botón para cancelar la edición
        Button cancelButton = new Button("Cancelar", e -> dialog.close());

        // Crear el layout para los campos del formulario
        FormLayout formLayout = new FormLayout(
                usernameField, nombreField,correoField,
                rolComboBox, imagePreview, upload, changePasswordButton, passwordField, newPasswordField
        );

        // Establecer el layout desplazable
        formLayout.setSizeFull();
        formLayout.setHeight("400px");  // Ajusta la altura según sea necesario

        // Layout para los botones
        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setSpacing(true);
        buttonLayout.setWidthFull();

        SplitLayout dialogLayout = new SplitLayout(formLayout, buttonLayout);
        dialogLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
        dialogLayout.setWidthFull();
        this.add(dialog);
        dialog.add(dialogLayout);
        dialog.setWidth("500px"); // Ajusta el ancho del diálogo según sea necesario
        dialog.open();
    }



    private Image createImage(byte[] foto) {
        if (foto == null || foto.length == 0) {
            return new Image("https://via.placeholder.com/50", "Sin imagen");
        }
        String base64Image = "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(foto);
        return new Image(base64Image, "Foto");
    }
}