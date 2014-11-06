exclude :test_exit_action, "kills test run"
exclude :test_hup_me, "kills test run"
exclude :test_kill_immediately_before_termination, "kills test run"
exclude :test_reserved_signal, "needs investigation"
exclude :test_signal2, "needs investigation"
exclude :test_signal_exception, "needs investigation"
exclude :test_signal_requiring, "needs investigation"
exclude :test_signame, "needs investigation"
exclude :test_trap, "needs investigation"
exclude :test_trap_puts, "needs investigation"
exclude :test_ignored_interrupt, "uses SIGINT which is already in use on the JVM"
exclude :test_trap_uncatchable_KILL, "uses SIGKILL which is already in use on the JVM"
exclude :test_trap_uncatchable_STOP, "uses SIGSTOP which is already in use on the JVM"
