import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Main {

    //мапа для сохранения результатов
    public static final Map<Integer, Integer> sizeToFreq = new HashMap<>();

    //счетчик для потоков
    public static int counter = 0;

    //количество потоков
    public static int allThreads = 1000;

    public static void main(String[] args) {

        //создаем 1000 потоков
        for (int i = 0; i < allThreads; i++) {
            new Thread(() -> {
                //получаем строку в каждом потоке
                String str = generateRoute("RLRFR", 100);
                int frequency = 0;
                for (int j = 0; j < str.length(); j++) {
                    //ищем количество символов "R"
                    if (str.charAt(j) == 'R') {
                        frequency++;
                    }
                }
                //синхронизируемся по мапе
                synchronized (sizeToFreq) {
                    //метод слияния: если встретилось одинаковое количество символов в разных строках,
                    // то к value мы прибавляем 1, методом Integer::sum
                    sizeToFreq.merge(frequency, 1, Integer::sum);
                    //увеличиваем счетчик потоков
                    counter++;
                    //освобождаем синхронизацию, для передачи главному потоку
                    sizeToFreq.notify();
                }
            }).start();
        }

        //синхронизируемся по мапе в главном потоке
        synchronized (sizeToFreq) {
            //пока все потоки не выполнились
            while (counter < allThreads) {
                try {
                    //главный поток ждет
                    sizeToFreq.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        //выполняем стрим по набору значений мапы
        sizeToFreq.entrySet().stream()
                //сортируем по value в обратном порядке
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                //вывод ограничиваем одним значением
                .limit(1)
                //выводим значение на экран
                .forEach(e ->
                        System.out.println("Самое частое количество повторений " + e.getKey() + " (встретилось " + e.getValue() + " раз)"));
        System.out.println("Другие размеры:");
        sizeToFreq.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                //пропускаем первое
                .skip(1)
                .forEach(e ->
                        System.out.println(" - " + e.getKey() + " (" + e.getValue() + " раз)"));
    }

    public static String generateRoute(String letters, int length) {
        Random random = new Random();
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < length; i++) {
            route.append(letters.charAt(random.nextInt(letters.length())));
        }
        return route.toString();
    }
}