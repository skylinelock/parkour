package mc.sky_lock.parkour.command;

import lombok.NonNull;
import mc.sky_lock.parkour.message.FailedMessage;
import mc.sky_lock.parkour.Parkour;
import mc.sky_lock.parkour.ParkourPlugin;
import mc.sky_lock.parkour.message.SuccessMessage;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author sky_lock
 */

class ActiveCommand implements ICommand {

    private final ParkourPlugin plugin;

    ActiveCommand(@NonNull ParkourPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, Command command, String label, String[] args) {
        if (args.length < 2) {
            player.sendMessage(FailedMessage.NOT_ENOUGH_ARGS.getText());
            return;
        }
        List<Parkour> parkours = plugin.getParkours();
        String inputId = args[1];

        for (Parkour parkour : parkours) {
            if (!parkour.getId().equals(inputId)) {
                continue;
            }
            String successMsg = SuccessMessage.ACTIVE.getText();
            if (!parkour.isActive()) {
                if (parkour.getStartPoint() == null || parkour.getEndPoint() == null || parkour.getRespawnPoint() == null || parkour.getName() == null) {
                    break;
                }
                parkour.setActive(true);
                player.sendMessage(successMsg);
                return;
            }
            parkour.setActive(false);
            player.sendMessage(successMsg);
            return;
        }
        player.sendMessage(FailedMessage.ACTIVE.getText());
    }
}
