package gub.rionegro.syslr.services;

import gub.rionegro.syslr.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuService {

    @Autowired
    private BotMenuRepository menuRepo;
    @Autowired
    private BotFlowRepo flowRepo;

    public String buildMenuMessage(String menuCode) {
        BotMenu menu = menuRepo.findByComando(menuCode).orElseThrow();
        List<BotFlow> flows = flowRepo.findByMenu(menu);

        StringBuilder sb = new StringBuilder();
        sb.append("*").append(menu.getTitulo()).append("*\n\n");
        sb.append(menu.getDescripcion()).append("\n\n");

        int i = 1;
        for (BotFlow flow : flows) {
            sb.append(i).append(") ").append(flow.getName()).append("\n");
            i++;
        }
        sb.append("\nResponda con el número de la opción.");
        return sb.toString();
    }

    public BotFlow getFlowByMenuSelection(String menuCode, int selection) {
        BotMenu menu = menuRepo.findByComando(menuCode).orElseThrow();
        List<BotFlow> flows = flowRepo.findByMenu(menu);

        if (selection < 1 || selection > flows.size()) {
            throw new IllegalArgumentException("Opción inválida");
        }
        return flows.get(selection - 1);
    }


    public BotMenu findByComando(String main) {
        return menuRepo.findByComando(main);
    }
}
