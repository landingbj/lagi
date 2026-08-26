package ai.servlet.api;

import ai.account.CuihuaAccountService;
import ai.account.CuihuaUser;

import java.sql.SQLException;

/** Resolves and authorizes the caller's token-usage query scope. */
public class TokenUsageAuthorizationService {
    public static final String CUIHUA_ACTOR_HEADER = "X-Cuihua-Actor-Id";

    private final CuihuaAccountService accountService;

    public TokenUsageAuthorizationService() {
        this(new CuihuaAccountService());
    }

    TokenUsageAuthorizationService(CuihuaAccountService accountService) {
        this.accountService = accountService;
    }

    public TokenUsageScope authorizeAuthenticatedUser(CuihuaUser actor, String targetUserId)
            throws SQLException, TokenUsageAuthorizationException {
        if (actor == null) {
            throw new TokenUsageAuthorizationException(Reason.AUTHENTICATION_REQUIRED);
        }
        return authorize(actor, targetUserId);
    }

    public TokenUsageScope authorizeCuihuaActor(String actorUserId, String targetUserId)
            throws SQLException, TokenUsageAuthorizationException {
        if (isBlank(actorUserId)) {
            throw new TokenUsageAuthorizationException(Reason.CUIHUA_ACTOR_REQUIRED);
        }
        CuihuaUser actor = accountService.findActiveUserById(actorUserId.trim());
        if (actor == null) {
            throw new TokenUsageAuthorizationException(Reason.CUIHUA_ACTOR_NOT_FOUND);
        }
        return authorize(actor, targetUserId);
    }

    private TokenUsageScope authorize(CuihuaUser actor, String rawTargetUserId)
            throws SQLException, TokenUsageAuthorizationException {
        if (!actor.hasRecognizedRole()) {
            throw new TokenUsageAuthorizationException(Reason.ACTOR_ROLE_NOT_ALLOWED);
        }
        String targetUserId = optionalText(rawTargetUserId);
        if (targetUserId == null) {
            if (actor.canViewAllTokenUsage()) {
                return TokenUsageScope.all(actor);
            }
            return TokenUsageScope.self(actor);
        }

        CuihuaUser targetUser = accountService.findActiveUserById(targetUserId);
        if (targetUser == null) {
            throw new TokenUsageAuthorizationException(Reason.TARGET_USER_NOT_FOUND);
        }
        if (actor.getUserId().equals(targetUser.getUserId())) {
            return TokenUsageScope.self(actor);
        }
        if (!actor.canViewAllTokenUsage()) {
            throw new TokenUsageAuthorizationException(Reason.ACCESS_DENIED);
        }
        return TokenUsageScope.user(actor, targetUser.getUserId());
    }

    private static String optionalText(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public enum Scope {
        SELF("self"),
        USER("user"),
        ALL("all");

        private final String value;

        Scope(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum Reason {
        AUTHENTICATION_REQUIRED,
        CUIHUA_ACTOR_REQUIRED,
        CUIHUA_ACTOR_NOT_FOUND,
        ACTOR_ROLE_NOT_ALLOWED,
        TARGET_USER_NOT_FOUND,
        ACCESS_DENIED
    }

    public static final class TokenUsageScope {
        private final CuihuaUser actor;
        private final Scope scope;
        private final String queryUserId;

        private TokenUsageScope(CuihuaUser actor, Scope scope, String queryUserId) {
            this.actor = actor;
            this.scope = scope;
            this.queryUserId = queryUserId;
        }

        private static TokenUsageScope self(CuihuaUser actor) {
            return new TokenUsageScope(actor, Scope.SELF, actor.getUserId());
        }

        private static TokenUsageScope user(CuihuaUser actor, String targetUserId) {
            return new TokenUsageScope(actor, Scope.USER, targetUserId);
        }

        private static TokenUsageScope all(CuihuaUser actor) {
            return new TokenUsageScope(actor, Scope.ALL, null);
        }

        public CuihuaUser getActor() {
            return actor;
        }

        public Scope getScope() {
            return scope;
        }

        /** Null means the DAO should query every user. */
        public String getQueryUserId() {
            return queryUserId;
        }

        public String getTargetUserId() {
            return queryUserId;
        }
    }

    public static final class TokenUsageAuthorizationException extends Exception {
        private final Reason reason;

        TokenUsageAuthorizationException(Reason reason) {
            super(reason.name());
            this.reason = reason;
        }

        public Reason getReason() {
            return reason;
        }
    }
}
