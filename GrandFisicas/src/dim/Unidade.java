package dim;

public class Unidade implements Comparable<Unidade>{
  private String nome;
  private String sigla;
  private String tipo;


  public Unidade(String nome, String sigla, String tipo) {
    this.nome = nome;
    this.sigla = sigla;
    this.tipo = tipo;

  }

  public Unidade() {
  }

  public String getNome() {
    return nome;
  }

  public String getSigla() {
    return sigla;
  }

  public String getTipo() {
    return tipo;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public void setSigla(String sigla) {
    this.sigla = sigla;
  }

  public void setTipo(String tipo) {
    this.tipo = tipo;
  }

  public int compareTo( Unidade unidade){
    return this.nome.compareTo(unidade.getNome());
  }


  public String toString(){
    return nome + ":" + sigla + ":" + tipo;
  }
}