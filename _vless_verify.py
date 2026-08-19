import json
import sys

import paramiko

HOST = "213.176.65.164"
SSH_USER = "root"
SSH_PASSWORD = "eLpD6D5QOVJM"


def safe_print(text):
    sys.stdout.buffer.write((text or "").encode("utf-8", errors="replace"))
    sys.stdout.buffer.write(b"\n")


def main():
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    client.connect(
        HOST,
        username=SSH_USER,
        password=SSH_PASSWORD,
        timeout=30,
        allow_agent=False,
        look_for_keys=False,
    )

    stdin, stdout, stderr = client.exec_command(
        "cat /usr/local/x-ui/bin/config.json", timeout=30
    )
    cfg = json.loads(stdout.read().decode())
    for ib in cfg.get("inbounds", []):
        if ib.get("port") == 443:
            safe_print(json.dumps(ib, indent=2, ensure_ascii=False))

    stdin2, stdout2, stderr2 = client.exec_command(
        "systemctl is-active x-ui; systemctl is-active xray 2>/dev/null; "
        "ss -tlnp | grep -E ':443|:2053'; "
        "curl -sk -o /dev/null -w 'reality_probe_http_code:%{http_code}\\n' https://127.0.0.1:443 || true",
        timeout=30,
    )
    safe_print(stdout2.read().decode())
    safe_print(stderr2.read().decode())

    client.close()


if __name__ == "__main__":
    main()
