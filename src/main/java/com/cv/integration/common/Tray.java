package com.cv.integration.common;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

@Slf4j
public class Tray {
    public void startup() throws AWTException {
        if (SystemTray.isSupported()) {
            log.info("Tray started.");
            SystemTray tray = SystemTray.getSystemTray();
            ImageIcon icon = new ImageIcon("icon/sync_inv.png");
            PopupMenu menu = new PopupMenu();
            MenuItem closeItem = new MenuItem("Exit");
            closeItem.addActionListener(e -> System.exit(0));
            menu.add(closeItem);
            TrayIcon trayIcon = new TrayIcon(icon.getImage(), "Core Care Sync Service", menu);
            trayIcon.setImageAutoSize(true);
            tray.add(trayIcon);
        }
    }
}
