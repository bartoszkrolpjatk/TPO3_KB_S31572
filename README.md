# Asynchroniczny Serwer Czatu (Java Virtual Threads)

**Uwaga:** Projekt został zrealizowany w ramach zadania akademickiego (projekt na studia).

---

## 🎯 Cel Projektu i Podsumowanie Rozwiązania

Głównym celem projektu było zaprojektowanie i implementacja asynchronicznego, wielowątkowego serwera i klienta czatu TCP, zdolnego do jednoczesnej obsługi wielu połączeń. 

Kluczowym założeniem technologicznym było wykorzystanie **Wątków Wirtualnych (Virtual Threads)** wprowadzonych w nowoczesnych wersjach języka Java (Project Loom). Zamiast polegać na klasycznych, "ciężkich" wątkach systemowych lub skomplikowanym mechanizmie `java.nio` (Selektory, Kanały), projekt wykorzystuje blokujące operacje I/O (`java.io.BufferedReader`, `java.io.PrintWriter`), które w kontekście lekkich wątków wirtualnych stają się wysoce skalowalne.

Dzięki zastosowaniu `Executors.newVirtualThreadPerTaskExecutor()`, serwer może bez spadku wydajności przydzielić dedykowany wątek do każdego połączonego klienta.

### Technologie:
* **Język:** Java (wersja wspierająca Virtual Threads)
* **Sieć:** `java.net.Socket`, `java.net.ServerSocket`
* **Współbieżność:** `Thread.ofVirtual()`, `ExecutorService`, `ReentrantLock`, `ConcurrentHashMap`
* **Testowanie:** `java.util.concurrent.FutureTask`, pliki konfiguracyjne YAML (biblioteka `SnakeYAML`)

---

## 📜 Protokół Komunikacyjny

Aplikacja wykorzystuje prosty, oparty na tekście protokół warstwy aplikacji. Każda wiadomość przesyłana między klientem a serwerem (w obie strony) zakończona jest znakiem nowej linii (`\n`) i musi być zgodna z góry ustalonym formatem, za którego sprawdzanie odpowiada `MessageValidator`.

**Struktura wiadomości:** `<OPERACJA>:<DANE>`

Obsługiwane operacje:
1.  **Logowanie:** `hi:<id_klienta>` 
    * *Przykład:* `hi:Adam`
    * Wysyłane przez klienta po nawiązaniu połączenia.
2.  **Wysłanie wiadomości:** `send:<treść_wiadomości>`
    * *Przykład:* `send:Cześć wszystkim!`
    * Wysyłane przez klienta w celu rozesłania tekstu do innych użytkowników.
3.  **Wylogowanie:** `bye:<id_klienta>`
    * *Przykład:* `bye:Adam`
    * Wysyłane przez klienta przy opuszczaniu czatu. Zamyka połączenie.
4.  **Zdarzenia serwera (Zastrzeżone):** `event:<treść>`
    * Wewnętrzna operacja używana przez serwer (rozgłaszanie powiadomień np. o wylogowaniu/zalogowaniu).

---

## 🔄 Komunikacja Klient-Serwer

Komunikacja jest **w pełni asynchroniczna i dwukierunkowa (full-duplex)**.

### Architektura Serwera (`ChatServer` & `ClientHandler`)
1.  **Akceptacja połączeń:** Główny wątek wirtualny serwera działa w nieskończonej pętli, nasłuchując na porcie (`serverSocket.accept()`).
2.  **Obsługa Klienta:** Po akceptacji połączenia, natychmiast uruchamiany jest nowy wątek wirtualny z puli egzekutora. Kontrolę nad gniazdem przejmuje klasa `ClientHandler`.
3.  **Zarządzanie Stanem:** Aktywne sesje przetrzymywane są w bezpiecznej wątkowo mapie (`ConcurrentHashMap` wewnątrz `ContextHolder`). 
4.  **Rozwiązywanie wyścigów (Race Conditions):** Rozsyłanie wiadomości (broadcast) oraz dodawanie klienta do kontekstu objęte jest blokadą `ReentrantLock`. Gwarantuje to zachowanie atomowości zdarzeń (brak rozjazdu chronologicznego w logach u różnych klientów). Zamek działa w trybie *niesprawiedliwym (unfair)*, maksymalizując przepustowość.
5.  **Graceful Shutdown:** Serwer obsługuje łagodne wyłączanie poprzez `awaitTermination`, gwarantując zamknięcie zasobów i rozesłanie wiadomości pożegnalnej przed zamknięciem głównego Socketu.

### Architektura Klienta (`ChatClient`)
1.  **Rozdział Obowiązków:** Klient unika blokowania aplikacji przez rozdzielenie wysyłania i odbierania na dwa osobne strumienie sterowania.
2.  **Wątek Nasłuchujący (Listener Thread):** W momencie logowania (`login()`), tworzony jest wątek wirtualny działający w tle (`listeningThread`). Jego jedynym zadaniem jest ciągłe czytanie strumienia wejściowego z serwera (`in.readLine()`) i zapisywanie wiadomości do lokalnego, zsynchronizowanego widoku czatu (`Log`).
3.  **Wysyłanie Danych:** Główny wątek używa metody `sendMessage()`, która formatuje dane zgodnie z protokołem i "wypycha" je do strumienia wyjściowego gniazda (`PrintWriter`).
4.  **Zamknięcie Sesji:** Zakończenie pracy odbywa się poprzez wysłanie komendy `bye`. Serwer odbiera ją, rozsyła broadcast pożegnalny i po swojej stronie zamyka gniazdo sieciowe. To z kolei rzuca sygnał końca strumienia (lub wyjątek) po stronie klienta, co pozwala pętli nasłuchującej naturalnie się zakończyć i zwolnić zasoby.
