package com.verang.verangvelocitylistplayers;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.scheduler.Scheduler;
import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Plugin(
        id = "verangvelocitylistplayers",
        name = "VerangVelocityListPlayers",
        version = "1.0.0",
        description = "A plugin to list players on each server in the proxy",
        url = "https://x.com/verangmc",
        authors = {"Verang"}
)
public class VerangVelocityListPlayersPlugin {

    private final ProxyServer server;
    private final Logger logger;

    @Inject
    public VerangVelocityListPlayersPlugin(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        server.getCommandManager().register("listproxy", new ListProxyCommand(server), "lproxy");
    }

    private static class ListProxyCommand implements SimpleCommand {

        private final ProxyServer server;

        public ListProxyCommand(ProxyServer server) {
            this.server = server;
        }

        @Override
        public void execute(Invocation invocation) {
            CommandSource source = invocation.source();
            Map<String, List<Player>> serverPlayers = new HashMap<>();

            for (Player player : server.getAllPlayers()) {
                Optional<ServerConnection> connection = player.getCurrentServer();
                connection.ifPresent(serverConn -> serverPlayers
                        .computeIfAbsent(serverConn.getServerInfo().getName(), k -> new java.util.ArrayList<>())
                        .add(player));
            }

            int totalPlayers = serverPlayers.values().stream().mapToInt(List::size).sum();

            Component header = Component.text("\n")
                    .append(Component.text("[!] ", TextColor.color(0x8B0000)))
                    .append(Component.text("There are ", NamedTextColor.WHITE))
                    .append(Component.text(totalPlayers, NamedTextColor.YELLOW))
                    .append(Component.text(" players online in proxy. The list is:", NamedTextColor.WHITE))
                    .append(Component.text("\n\n"));

            source.sendMessage(header);

            serverPlayers.forEach((serverName, players) -> {
                Component serverHeader = Component.text("[" + serverName + "] ", NamedTextColor.GREEN)
                        .append(Component.text("(" + players.size() + "): ", NamedTextColor.YELLOW));

                Component playerList = Component.empty();
                for (int i = 0; i < players.size(); i++) {
                    playerList = playerList.append(Component.text(players.get(i).getUsername(), NamedTextColor.WHITE));
                    if (i < players.size() - 1) {
                        playerList = playerList.append(Component.text(", ", NamedTextColor.WHITE));
                    }
                }

                source.sendMessage(serverHeader.append(playerList));
            });

            source.sendMessage(Component.text(" "));
        }


        @Override
        public List<String> suggest(Invocation invocation) {
            return java.util.Collections.emptyList();
        }

        @Override
        public boolean hasPermission(Invocation invocation) {
            return invocation.source().hasPermission("velocitylistproxy.list");
        }
    }
}
