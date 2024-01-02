import java.sql.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/postgres";
        String user = "postgres";
        String password = "2626152";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            if (conn != null) {
                System.out.println("Connected to the database!");

                // Создание таблицы книг
                String createTableSql = "CREATE TABLE IF NOT EXISTS books (" +
                        "id SERIAL PRIMARY KEY," +
                        "title VARCHAR(255)," +
                        "author VARCHAR(255)," +
                        "year INT," +
                        "genre VARCHAR(255)," +
                        "rating DOUBLE PRECISION," +
                        "hashtags VARCHAR(255)," +
                        "description TEXT" +
                        ")";

                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(createTableSql);
                } catch (SQLException ex) {
                    System.out.println("Ошибка при создании таблицы: " + ex.getMessage());
                }


// Создание таблицы заемщиков
                String createBorrowersTableSql = "CREATE TABLE IF NOT EXISTS borrowers (id SERIAL PRIMARY KEY, name TEXT, book_id INTEGER REFERENCES books(id))";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(createBorrowersTableSql);
                }

// Добавление столбца returned_date, если его нет
                String addReturnedDateColumnSql = "ALTER TABLE history ADD COLUMN returned_date TIMESTAMP";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(addReturnedDateColumnSql);
                } catch (SQLException e) {
                    // Обработка ошибки, если столбец уже существует или возникла другая проблема
                    System.out.println("Ошибка при добавлении столбца returned_date: " + e.getMessage());
                }

// Создание таблицы истории
                String createHistoryTableSql = "CREATE TABLE IF NOT EXISTS history (id SERIAL PRIMARY KEY, borrower_name TEXT, book_id INTEGER REFERENCES books(id), borrowed_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(createHistoryTableSql);
                }



                Scanner scanner = new Scanner(System.in);
                int choice = 0;

                while (choice != 4) {
                    System.out.println("Меню:");
                    System.out.println("1. Регистрация книги");
                    System.out.println("2. Взять книгу");
                    System.out.println("3. Вернуть книгу");
                    System.out.println("4. Выход");
                    System.out.print("Выберите действие: ");
                    choice = scanner.nextInt();

                    switch (choice) {
                        case 1:
                            System.out.print("Введите название книги: ");
                            scanner.nextLine(); // очистка буфера ввода
                            String title = scanner.nextLine();

                            System.out.print("Введите автора книги: ");
                            String author = scanner.nextLine();

                            System.out.print("Введите год издания книги: ");
                            int year = scanner.nextInt();
                            scanner.nextLine(); // очистка буфера ввода

                            System.out.print("Введите жанр книги: ");
                            String genre = scanner.nextLine();

                            System.out.print("Введите оценку книги: ");
                            double rating = scanner.nextDouble();
                            scanner.nextLine(); // очистка буфера ввода

                            System.out.print("Введите хештеги книги: ");
                            String hashtags = scanner.nextLine();

                            System.out.print("Введите краткое описание книги: ");
                            String description = scanner.nextLine();

// ... код для регистрации книги
                            String insertSql = "INSERT INTO books (title, author, year, genre, rating, hashtags, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
                            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                                pstmt.setString(1, title);
                                pstmt.setString(2, author);
                                pstmt.setInt(3, year);
                                pstmt.setString(4, genre);
                                pstmt.setDouble(5, rating);
                                pstmt.setString(6, hashtags);
                                pstmt.setString(7, description);
                                pstmt.executeUpdate();
                                System.out.println("Книга успешно зарегистрирована!");
                            } catch (SQLException ex) {
                                System.out.println("Ошибка при регистрации книги: " + ex.getMessage());
                            }


                        case 2:

                            // Взять книгу
                            System.out.print("Введите ваше имя: ");
                            scanner.nextLine(); // Consume newline
                            String borrowerName = scanner.nextLine();
                            System.out.print("Введите ID книги, которую вы хотите взять: ");
                            int bookId;
                            if (scanner.hasNextInt()) {
                                bookId = scanner.nextInt();

                                // Проверка, не взята ли уже книга
                                String checkBorrowedSql = "SELECT is_borrowed FROM books WHERE id = ?";
                                try (PreparedStatement pstmt = conn.prepareStatement(checkBorrowedSql)) {
                                    pstmt.setInt(1, bookId);
                                    ResultSet rs = pstmt.executeQuery();
                                    if (rs.next()) {
                                        boolean isBorrowed = rs.getBoolean("is_borrowed");
                                        if (isBorrowed) {
                                            System.out.println("Эта книга уже взята другим пользователем.");
                                            break;
                                        }
                                    } else {
                                        System.out.println("Книга с указанным ID не найдена.");
                                        break;
                                    }
                                } catch (SQLException ex) {
                                    System.out.println("Ошибка при проверке статуса книги: " + ex.getMessage());
                                    break;
                                }

                                // Продолжаем операцию, книга доступна для взятия
                                String borrowSql = "UPDATE books SET is_borrowed = TRUE WHERE id = ?";
                                try (PreparedStatement pstmt = conn.prepareStatement(borrowSql)) {
                                    pstmt.setInt(1, bookId);
                                    pstmt.executeUpdate();
                                } catch (SQLException ex) {
                                    System.out.println("Ошибка при взятии книги: " + ex.getMessage());
                                    break;
                                }

                                // ... остальной код оставляем без изменений
                                // добавление записей о заемщике и в историю
                                // ...

                                System.out.println("Вы успешно взяли книгу!");
                            } else {
                                System.out.println("Вы ввели неверные данные. Пожалуйста, введите число.");
                            }
                            break;
                        case 3:


                            // Вернуть книгу
                            scanner.nextLine(); // очистка буфера ввода
                            System.out.print("Введите ваше имя: ");
                            String borrowerNameReturn = scanner.nextLine();

                            System.out.print("Введите ID книги, которую вы хотите вернуть: ");
                            int returnBookId;
                            if (scanner.hasNextInt()) {
                                returnBookId = scanner.nextInt();

                                // Проверка, взята ли книга этим заемщиком
                                String checkBookBorrowedSql = "SELECT * FROM borrowers WHERE book_id = ? AND name = ?";
                                try (PreparedStatement pstmt = conn.prepareStatement(checkBookBorrowedSql)) {
                                    pstmt.setInt(1, returnBookId);
                                    pstmt.setString(2, borrowerNameReturn);
                                    ResultSet rs = pstmt.executeQuery();
                                    if (!rs.next()) {
                                        System.out.println("Книга с указанным ID не была взята вами.");
                                        break;
                                    }
                                } catch (SQLException ex) {
                                    System.out.println("Ошибка при проверке статуса книги: " + ex.getMessage());
                                    break;
                                }

                                // Обновление статуса книги на "не взята"
                                String updateBookStatusSql = "UPDATE books SET is_borrowed = FALSE WHERE id = ?";
                                try (PreparedStatement pstmt = conn.prepareStatement(updateBookStatusSql)) {
                                    pstmt.setInt(1, returnBookId);
                                    pstmt.executeUpdate();
                                } catch (SQLException ex) {
                                    System.out.println("Ошибка при обновлении статуса книги: " + ex.getMessage());
                                    break;
                                }

                                // Удаление записи о взятии книги из таблицы borrowers
                                String deleteBorrowerRecordSql = "DELETE FROM borrowers WHERE book_id = ? AND name = ?";
                                try (PreparedStatement pstmt = conn.prepareStatement(deleteBorrowerRecordSql)) {
                                    pstmt.setInt(1, returnBookId);
                                    pstmt.setString(2, borrowerNameReturn);
                                    pstmt.executeUpdate();
                                } catch (SQLException ex) {
                                    System.out.println("Ошибка при удалении информации о взятии книги: " + ex.getMessage());
                                    break;
                                }

                                // Запись информации о возврате книги в таблицу истории
                                String updateHistorySql = "UPDATE history SET returned_date = CURRENT_TIMESTAMP WHERE book_id = ? AND borrower_name = ?";
                                try (PreparedStatement pstmt = conn.prepareStatement(updateHistorySql)) {
                                    pstmt.setInt(1, returnBookId);
                                    pstmt.setString(2, borrowerNameReturn);
                                    pstmt.executeUpdate();
                                } catch (SQLException ex) {
                                    System.out.println("Ошибка при обновлении информации о возврате книги в истории: " + ex.getMessage());
                                    break;
                                }

                                System.out.println("Вы успешно вернули книгу и информация о возврате записана в историю!");
                            } else {
                                System.out.println("Вы ввели неверные данные. Пожалуйста, введите число.");
                            }
                            break;
















                        case 4:
                            System.out.println("Программа завершена.");
                            break;

                        default:
                            System.out.println("Выберите правильную опцию.");
                            break;
                    }
                }
            } else {
                System.out.println("Failed to connect to the database!");
            }

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
