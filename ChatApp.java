import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class ChatApp {
  private static final int PORT = 6789;
  private static final int BUFFER_SIZE = 1024;
  private static final Scanner scan = new Scanner(System.in);
  private static final Thread thread = new Thread(ChatApp::receiveMessages);

  private static MulticastSocket mSocket;
  private static InetSocketAddress group;

  private static void sendMessage(String message) {
    try {
      byte[] userBytes = message.getBytes();
      DatagramPacket messageOut =
          new DatagramPacket(userBytes, userBytes.length, group.getAddress(), PORT);
      mSocket.send(messageOut);
    } catch (IOException e) {
      System.err.println("Falha ao enviar messagem. Erro: " + e.getMessage());
    }
  }

  private static void receiveMessages() {
    try {
      while (!mSocket.isClosed()) {
        DatagramPacket messageIn = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
        mSocket.receive(messageIn);
        System.out.println(new String(messageIn.getData(), 0, messageIn.getLength()));
      }
    } catch (IOException e) {
      if (!e.getClass().equals(SocketException.class)) {
        System.err.println("Falha ao receber messagem. Erro: " + e.getMessage());
      }
    }
  }

  private static boolean enterRoom(String ip) {
    try {
      mSocket = new MulticastSocket(PORT);
      InetAddress groupIp = InetAddress.getByName(ip);
      group = new InetSocketAddress(groupIp, PORT);
      mSocket.joinGroup(group, null);
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  private static boolean leaveRoom() {
    try {
      scan.close();
      mSocket.leaveGroup(group, null);
      mSocket.close();
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  public static void main(String[] args) {
    if (args.length < 2) {
      System.err.println("Nome de usuário e/ou endereço de IP faltantes...");
      return;
    }

    final String user = args[0];
    final String ip = args[1];

    if (enterRoom(ip)) {
      thread.start();
      sendMessage(user + " entrou na sala");
    } else {
      System.err.println("Não foi possível entrar na sala");
      return;
    }

    System.out.println("Digite sua mensagem: (Digite SAIR caso deseje sair)");
    String message = scan.nextLine();
    while (!message.equalsIgnoreCase("SAIR")) {
      sendMessage(user + ": " + message);
      message = scan.nextLine();
    }
    sendMessage(user + " saiu da sala");

    if (!leaveRoom()) {
      System.err.println("Não foi possível sair corretamente da sala");
    }
  }
}
