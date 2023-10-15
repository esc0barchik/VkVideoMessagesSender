package org.example;

import com.journaldev.utils.ApiMessageSender;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class UIWindow {

  public static void main(String[] args) {
    JFrame frame = new JFrame("Отправка видеосообщения");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(400, 210);

    // Создаем панель для размещения компонентов
    JPanel panel = new JPanel();
    panel.setLayout(new GridLayout(5, 1));

    JLabel domainText = new JLabel("Введите домен:");
    JTextField domainInput = new JTextField("api.vk.me");
    JLabel peerIdText = new JLabel("Введите Peer ID:");
    JTextField peerIdInput = new JTextField();
    JLabel tokenText = new JLabel("Введите токен:");
    JTextField tokenInput = new JTextField();
    JLabel fileLabel = new JLabel("Выберите видео:");
    JTextField fileField = new JTextField("Можно через Drag-and-drop");
    fileField.setEditable(false);
    JButton fileButton = new JButton("Выбрать видео");
    JButton submitButton = new JButton("Отправить");
    JLabel resultLabel = new JLabel();
    JButton copyButtonForError = new JButton("Скопировать");

    panel.add(domainText);
    panel.add(domainInput);
    panel.add(peerIdText);
    panel.add(peerIdInput);
    panel.add(tokenText);
    panel.add(tokenInput);
    panel.add(fileLabel);
    panel.add(fileField);
    panel.add(fileButton);
    panel.add(submitButton);
    panel.add(copyButtonForError);
    copyButtonForError.setVisible(false);

    // Прослушивание событий инпута файлов
    fileButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
          File selectedFile = fileChooser.getSelectedFile();
          fileField.setText(selectedFile.getAbsolutePath());
        }
      }
    });

    // Прослушивание событий кнопки отправки драг-н-дропа
    DropTarget dropTarget = new DropTarget(fileField, new DropTargetAdapter() {
      @Override
      public void dragEnter(DropTargetDragEvent dtde) {
        fileField.setBackground(Color.LIGHT_GRAY);
      }

      @Override
      public void dragExit(DropTargetEvent dte) {
        fileField.setBackground(Color.WHITE);
      }

      @Override
      public void drop(DropTargetDropEvent dtde) {
        try {
          dtde.acceptDrop(DnDConstants.ACTION_COPY);
          List<File> droppedFiles = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
          if (!droppedFiles.isEmpty()) {
            File selectedFile = droppedFiles.get(0);
            fileField.setText(selectedFile.getAbsolutePath());
          }
          fileField.setBackground(Color.WHITE);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    });

    copyButtonForError.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String textToCopy = ApiMessageSender.getLogs();
        StringSelection selection = new StringSelection(textToCopy);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, null);
      }
    });

    // Прослушивание событий кнопки отправки
    submitButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String peerId = peerIdInput.getText();
        String domain = domainInput.getText();
        String token = tokenInput.getText();
        String filePath = fileField.getText();
        try {
          copyButtonForError.setVisible(false);
          resultLabel.setText(
              "msgID: " + ApiMessageSender.sendVideoMsg(peerId, token, filePath, domain));
        } catch (Exception error) {
          resultLabel.setText("Что-то пошло не так. Логи запросов:");
          copyButtonForError.setVisible(true);
        }
      }
    });

    // Панель для вывода результатов
    JPanel resultPanel = new JPanel();
    resultPanel.add(resultLabel);
    resultPanel.add(copyButtonForError);

    // Главная панель с кнопками
    JPanel contentPanel = new JPanel();
    contentPanel.setLayout(new BorderLayout());
    contentPanel.add(panel, BorderLayout.NORTH);
    contentPanel.add(resultPanel, BorderLayout.CENTER);

    // Добавляем главную панель
    frame.add(contentPanel);

    frame.setVisible(true);
  }
}
