package com.alura.screenmatch.principal;

import com.alura.screenmatch.models.*;
import com.alura.screenmatch.repository.SerieRepository;
import com.alura.screenmatch.service.ConsumoAPI;
import com.alura.screenmatch.service.ConverteDados;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private ConsumoAPI consumoAPI =  new ConsumoAPI();
    private final String ENDERECO="https://www.omdbapi.com/?t=";
    private final String API_KEY="&apikey=f25e766b";

    private ConverteDados conversor = new ConverteDados();
    private Scanner leitura =  new Scanner(System.in);
    private List<DadosSerie> dadosSerie = new ArrayList<>();

    private SerieRepository repository;

    private List<Serie> series = new ArrayList<>();

    private Optional<Serie> serieBusca;

    public Principal(SerieRepository repository){
        this.repository = repository;
    }

    public void exibeMenu() {
        var opcao = -1;

        while (opcao !=0) {
            var menu = """
                    1 - busca serie
                    2 - busca episodio
                    3 - Lista de series Buscadas
                    4 - Buscar serie da lista
                    5 - Buscar serie da lista por Ator
                    6 - Top 5 series da lista
                    7 - Busca serie da lista por genero
                    8 - Filtrar series
                    9 - Buscar episodio por trecho
                    10- Top 5 episodios da serie
                    11- Episodios a partir de uma data      
                    0 - sair
                    """;
            System.out.println(menu);
            opcao=leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscaSerieWeb();
                    break;
                case 2:
                    buscaPorEpisodioPorSerie();
                    break;
                case 3 :
                    listarSeriesBuscadas();
                    break;
                case 4 :
                    BuscarSerieDaLista();
                    break;
                case 5 :
                    BuscarSeriePorAtor();
                    break;
                case 6 :
                    BuscarTopSeries();
                    break;
                case 7 :
                    BuscaSeriePorGenero();
                    break;
                case 8 :
                    filtrarSeriesPorTemporadaEAvaliacao();
                    break;
                case 9 :
                    buscarEpisodioPorTrecho();
                    break;
                case 10 :
                    topFiveEpisodiosDaSerie();
                    break;
                case 11 :
                    buscarEpisodiosPorData();
                case 0:
                    System.out.println("Saindo..");
                    break;
                default:
                    System.out.println("opcao invalida");
            }
        }
    }
    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumoAPI.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }
    public void buscaSerieWeb() {
        DadosSerie dados =getDadosSerie();
        System.out.println(dados);
        Serie serie = new Serie(dados);
        repository.save(serie);
//        dadosSerie.add(dados);
        System.out.println(dados);
    }
    public void buscaPorEpisodioPorSerie() {
        listarSeriesBuscadas();
        System.out.println("Escolha uma série: ");
        var nomeSerie = leitura.nextLine();

        Optional<Serie>serie =series.stream().filter(s-> s.getTitulo().toLowerCase().contains(nomeSerie.toLowerCase()))
                .findFirst();

        if(serie.isPresent()) {

            var serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumoAPI.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios =temporadas.stream().flatMap(d-> d.episodios().stream()
                    .map(e-> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);
            repository.save(serieEncontrada);
        } else {
            System.out.println("Serie não encontrada");
        }
    }
    public void listarSeriesBuscadas() {
        series = repository.findAll();
        series.stream().sorted(Comparator.comparing(Serie::getGenero)).forEach(System.out::println);
    }

    private void BuscarSerieDaLista(){
        System.out.println("Escolha uma série: ");
        var nomeSerie = leitura.nextLine();
        serieBusca = repository.findByTituloContainingIgnoreCase(nomeSerie);
        if(serieBusca.isPresent()){
            System.out.println("Dados da serie "+serieBusca.get());
        }else {
            System.out.println("Serie não encontrada");
        }
    }

    private void BuscarSeriePorAtor(){
        System.out.println("Digite o nome do Ator: ");
        var nomeAtor = leitura.nextLine();

        System.out.println("qual critério de avaliacao: ");
        var avali = leitura.nextDouble();

        List<Serie> seriesEncontradas = repository.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, avali);
        System.out.println("Series do "+nomeAtor+" atuou");
        seriesEncontradas.forEach(s-> System.out.println(s.getTitulo() + " avaliacao "+s.getAvaliacao()));
    }

    private void BuscarTopSeries(){
        List<Serie> seriesTop = repository.findTop5ByOrderByAvaliacaoDesc();

        seriesTop.forEach(s-> System.out.println(s.getTitulo() + " Avaliacao "+ s.getAvaliacao()));
    }

    private void BuscaSeriePorGenero(){
        System.out.println("Digite o genero da serie a buscar: ");
        var serieGenero = leitura.nextLine();

        Categoria categoria = Categoria.fromPrompt(serieGenero);
        List<Serie> seriesPorCategoria = repository.findByGenero(categoria);
        System.out.println("Genero "+ serieGenero);
        seriesPorCategoria.forEach(System.out::println);
    }

    //Código omitido

    private void filtrarSeriesPorTemporadaEAvaliacao(){
        System.out.println("Filtrar séries até quantas temporadas? ");
        var totalTemporadas = leitura.nextInt();
        leitura.nextLine();
        System.out.println("Com avaliação a partir de que valor? ");
        var avaliacao = leitura.nextDouble();
        leitura.nextLine();
        List<Serie> filtroSeries = repository.seriesPorTemporadaEAValiacao(totalTemporadas, avaliacao);
        System.out.println("*** Séries filtradas ***");
        filtroSeries.forEach(s ->
                System.out.println(s.getTitulo() + "  - avaliação: " + s.getAvaliacao()));
    }

    private void buscarEpisodioPorTrecho(){
        System.out.println("Digite o trecho do nome do episodio");
        var trechoNome = leitura.nextLine();

        List<Episodio> episodiosEncontrados = repository.episodiosPorTrecho(trechoNome);
        episodiosEncontrados.forEach(e -> System.out.println("Titulo "+e.getTitulo()+", Serie "+e.getSerie().getTitulo() +" Episodio "+e.getNumeroEpisodio()+ " Temporada "+e.getTemporada()));
    }

    private void topFiveEpisodiosDaSerie(){
        BuscarSerieDaLista();
        if(serieBusca.isPresent()){
            Serie serie =serieBusca.get();
            List<Episodio> topEpisodios =repository.topEpisodiosPorSerie(serie);
            topEpisodios.forEach(e -> System.out.printf("""
                    Serie: %s  Temporada: %s Episodio: %s Nota: %s\n
                    """,e.getSerie().getTitulo(),e.getTemporada(),e.getNumeroEpisodio(),e.getAvaliacao()));
        }
    }

    private void buscarEpisodiosPorData(){
        BuscarSerieDaLista();
        if(serieBusca.isPresent()){
            System.out.println("Digite da data de inicio");
            var dataAno = leitura.nextLine();
            Serie serie = serieBusca.get();

            List<Episodio> episodiosAno = repository.episodiosPorSerieEAno(serie,dataAno);
            episodiosAno.forEach(System.out::println);
        }
    }

}
