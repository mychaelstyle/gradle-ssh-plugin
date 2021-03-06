package org.hidetake.gradle.ssh.api.command
/**
 * Represents stream interaction with the server.
 *
 * @author hidetake.org
 */
interface Interaction {
    /**
     * Wildcard for condition expression.
     */
    static final _ = Wildcard.instance

    static final standardOutput = Stream.StandardOutput

    static final standardError = Stream.StandardError

    /**
     * Get the standard input for the remote command.
     *
     * @return output stream
     */
    OutputStream getStandardInput()

    /**
     * Declare an interaction rule.
     *
     * @param condition map of condition
     * @param action the action performed if condition satisfied
     */
    void when(Map condition, Closure action)
}
