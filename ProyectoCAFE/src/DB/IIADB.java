package DB;

import java.sql.*;
public class IIADB {
    private Connection conn = null;
    private PreparedStatement ps = null;

    public IIADB() throws Exception {
        try{
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String url="jdbc:sqlserver://servidoriia.database.windows.net:1433;database=BDIIA;user=admin12@servidoriia;password=universidaddeHuelva12_;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";
            conn = DriverManager.getConnection(url);
            System.out.println("Conexion realizada");
        }
        catch(SQLException e){
            System.out.println("ERROR EN LA CONEXION "+e.getMessage());
            //JOptionPane.showMessageDialog(null,"No se ha podido establecer la conexión correctamene","ERROR",JOptionPane.ERROR, new ImageIcon("src/Aplicacion/img/desconexion.png"));
        }
    }

    public void desconexion() throws SQLException {
        try{
            conn.close();
            System.out.println("Desconexion realizada.\n");
        }
        catch(SQLException e){
            System.out.println("ERROR EN LA DESCONEXION"+e.getMessage());
        }
    }

    public boolean realizarConsultaBebida(String sqlQuery) throws SQLException {
        ps=conn.prepareStatement(sqlQuery);
        ResultSet res=ps.executeQuery();
        String []consulta= sqlQuery.split(" ");
        String nombreTa=consulta[3];
        String nombreBebida=consulta[7];
        boolean existe=res.next();
        ps.close();

        if(existe){
            System.out.println("Se ha encontrado " + nombreBebida + " en el almacén.");
            ps=conn.prepareStatement("UPDATE " +nombreTa+ " SET stock = stock - 1" + " where Nombre="+nombreBebida);
            ps.executeUpdate();
            ps.close();
            return true;
        }
        else{
            System.out.println("No se ha encontrado " + nombreBebida + " en el almacén.");
        }


        return false;
    }

    public String realizarConsultaAlumno(String sqlQuery) throws SQLException {
        ps=conn.prepareStatement(sqlQuery);
        System.out.println(sqlQuery);
        ResultSet res=ps.executeQuery();
        res.next();
        String email = res.getString(1).trim();
        ps.close();
        return email;
    }
}