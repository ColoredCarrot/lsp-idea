package info.voidev.lspidea.connect

enum class LspStatus {
    NOT_STARTED,

    STARTING,
    UNINITIALIZED,

    INITIALIZING,

    /**
     * The main "active" state of a session.
     * Communication channels are open and the server is ready to
     * process requests.
     */
    ACTIVE,

    /**
     * The server has been notified that we wish to end the session.
     */
    FINALIZING,

    /**
     * Intermediate state reached right after the server has responded
     * to the finalization request.
     */
    FINALIZED,

    STOPPING,
    STOPPED,
}
