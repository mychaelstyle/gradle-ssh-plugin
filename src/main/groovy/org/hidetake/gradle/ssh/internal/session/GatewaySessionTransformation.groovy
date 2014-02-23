package org.hidetake.gradle.ssh.internal.session

import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.SessionSpec

/**
 * Transformation for gateway session support.
 *
 * @author hidetake.org
 */
class GatewaySessionTransformation {
    protected static final LOCALHOST = '127.0.0.1'

    /**
     * Port forwarding tunnel.
     */
    static class Tunnel {
        final Remote endpoint
        final SessionSpec spec

        Tunnel(Remote target, Remote gateway) {
            endpoint = cloneRemote(target)
            spec = new SessionSpec(gateway, {
                endpoint.port = forwardLocalPortTo(target.host, target.port)
            })
        }

        private static cloneRemote(Remote remote) {
            new Remote(remote.name, remote.user, 0, LOCALHOST, null,
                    remote.password, remote.identity, remote.passphrase, remote.agent, remote.roles)
        }
    }

    /**
     * Applies transformation.
     * <p>
     * If the session has remote gateway property,
     * a forwarder session is inserted before the session.
     *
     * @param specs list of session spec
     * @return transformed list
     */
    static List<SessionSpec> transform(List<SessionSpec> specs) {
        specs.collect(transformer).flatten() as List<SessionSpec>
    }

    private static transformer = { SessionSpec spec ->
        if (spec.remote.gateway) {
            def specs = []
            def endpoint = generateTunnels(spec.remote, specs)
            specs << new SessionSpec(endpoint, spec.operationClosure)
        } else {
            spec
        }
    }

    /**
     * Recursive generator of tunnels.
     *
     * e.g.
     * <code><pre>
     *     // Client -> E -> F -> G -> T
     *     def tunnelF = new Tunnel(target.gateway.gateway, target.gateway.gateway.gateway)
     *     def tunnelG = new Tunnel(target.gateway, tunnelF.endpoint)
     *     def tunnelT = new Tunnel(target, tunnelG.endpoint)
     * </pre></code>
     *
     * @param target target remote host
     * @param specs tunnels
     * @return endpoint of tunnels
     */
    private static generateTunnels(Remote target, List<SessionSpec> specs) {
        if (target.gateway) {
            def tunnel = new Tunnel(target, generateTunnels(target.gateway, specs))
            specs << tunnel.spec
            tunnel.endpoint
        } else {
            target
        }
    }
}
