package ec.edu.epn.eda2.juez.validador;
import ec.edu.epn.eda2.juez.core.modelo.Arista;
import ec.edu.epn.eda2.juez.core.modelo.CasoGrafo;
import ec.edu.epn.eda2.juez.core.modelo.TipoGrafo;


import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Verifcador {



    public boolean validarRecorrido(List<Integer> respuestaEstudiante, String ordenReferencia){
        if (respuestaEstudiante != null && ordenReferencia != null && !ordenReferencia.equalsIgnoreCase("NO_APLICA")){
            List<Integer> ordenReferenciaLista = new ArrayList<>();
            StringTokenizer st = new StringTokenizer(ordenReferencia, ","); 

            while(st.hasMoreTokens()){
                ordenReferenciaLista.add(Integer.parseInt(st.nextToken()));
            }

            return respuestaEstudiante.equals(ordenReferenciaLista);
        }
        
        return false; 
    }

    public boolean validarOrdenTopologico(CasoGrafo g, List<Integer> respuestaEstudiante, String estadoReferencia){
        if(estadoReferencia.equalsIgnoreCase("NO_APLICA")){
            return true;
        }else if(estadoReferencia.equalsIgnoreCase("CICLO")){
            return (respuestaEstudiante != null && respuestaEstudiante.isEmpty())? true : false;
        }

        if(respuestaEstudiante != null && respuestaEstudiante.size() == g.vertices()){
            int[] posicion = new int[g.vertices()]; 
            boolean[] visto = new boolean[g.vertices()];

            for (int i = 0; i <= respuestaEstudiante.size() - 1; i++) {
                int nodoActual = respuestaEstudiante.get(i);
                
                if(nodoActual < 0 || nodoActual >= g.vertices() || visto[nodoActual]) return false;

                visto[nodoActual] = true;
                posicion[nodoActual] = i;
            }

            for(Arista arista : g.aristas()){
                if(posicion[arista.origen()] > posicion[arista.destino()])return false;
            }

            return true;
        }


        return false;
    }

    public boolean validarMST(CasoGrafo g, int pesoEstudiante, String pesoReferencia){
        if(pesoReferencia == null){
            return false;
        }else if(pesoReferencia.equalsIgnoreCase("NO_APLICA")){
            return true; 
        }

        int pesoEsperado = Integer.parseInt(pesoReferencia.trim()); 
        return pesoEsperado == pesoEstudiante; 
    }
}
