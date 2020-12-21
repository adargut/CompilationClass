package semanticanalysis;

public class SemanticException extends RuntimeException {
    private final SemanticError errorCode;

    public SemanticException(SemanticError errorCode) {
        super(errorCode.getErrorMsg());
        this.errorCode = errorCode;
    }

    public SemanticException(SemanticError errorCode, Object[] formatArguments) {
        super(String.format(errorCode.getErrorMsg(), formatArguments));
        this.errorCode = errorCode;
    }

    public SemanticException(String errorMessage, SemanticError errorCode) {
        super(errorMessage);
        this.errorCode = errorCode;
    }

    public SemanticException(String errorMessage, Throwable err, SemanticError errorCode) {
        super(errorMessage, err);
        this.errorCode = errorCode;
    }

    public SemanticException(SemanticError errorCode, Throwable err) {
        super(errorCode.getErrorMsg(), err);
        this.errorCode = errorCode;
    }

    public SemanticException(SemanticError errorCode, Throwable err, Object[] formatArguments) {
        super(String.format(errorCode.getErrorMsg(), formatArguments), err);
        this.errorCode = errorCode;
    }


    public SemanticError getErrorCode() {
        return this.errorCode;
    }
}


