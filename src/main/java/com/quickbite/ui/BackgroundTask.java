package com.quickbite.ui;

import com.quickbite.exception.DataAccessException;
import com.quickbite.exception.DomainException;
import com.quickbite.exception.NotFoundException;
import com.quickbite.exception.ValidationException;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.awt.Component;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * Runs database work off the Swing thread (EDT) so the window never freezes.
 * work runs in doInBackground(); onSuccess and error messages run in done(), back on the EDT.
 */
public final class BackgroundTask {
    private BackgroundTask() {
    }

    /**
     * @param button    disabled while the work runs (prevents double-clicks); may be null
     * @param onFailure runs after a failed action so the screen shows saved data; may be null
     */
    public static <T> void run(Component parent, JButton button, Callable<T> work,
                               Consumer<T> onSuccess, Runnable onFailure) {
        if (button != null) {
            button.setEnabled(false);
        }
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return work.call();                          // background thread
            }

            @Override
            protected void done() {                          // Swing thread
                try {
                    onSuccess.accept(get());
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    showError(parent, cause);
                    // If the database is down, a reload cannot help, so skip it.
                    if (onFailure != null && !(cause instanceof DataAccessException)) {
                        onFailure.run();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    if (button != null) {
                        button.setEnabled(true);
                    }
                }
            }
        }.execute();
    }

    /** Turns an exception into a plain message for the cashier. */
    public static void showError(Component parent, Throwable error) {
        String message;
        if (error instanceof ValidationException || error instanceof DomainException
                || error instanceof NotFoundException) {
            message = error.getMessage();
        } else if (error instanceof DataAccessException) {
            error.printStackTrace();                          // details for the developer
            message = "Cannot reach the database. Try again.";
        } else {
            error.printStackTrace();
            message = "Unexpected error: " + error;
        }
        JOptionPane.showMessageDialog(parent, message, "QuickBite", JOptionPane.WARNING_MESSAGE);
    }
}
