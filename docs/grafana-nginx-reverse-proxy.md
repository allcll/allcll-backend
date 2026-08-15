# Grafana Nginx Reverse Proxy 적용안

이 문서는 `grafana.allcll.kr`을 기존 EC2 Nginx와 Certbot으로 공개하기 위한 검토용 로컬 초안이다. 아직 EC2, 가비아 DNS, AWS 보안 그룹에 적용하지 않는다.

## 전제

- Nginx는 이미 `allcll.kr`, `admin.allcll.kr`에서 HTTPS를 제공한다.
- Grafana Docker는 적용 뒤 `127.0.0.1:3000`에서만 수신한다.
- `grafana.allcll.kr` A 레코드가 EC2 공인 IP를 가리킨 뒤에만 인증서를 발급한다.
- Let’s Encrypt 인증서는 기존 Certbot Nginx 플러그인으로 발급한다. 발급 뒤 Certbot이 생성·수정한 block은 아래 최종 설정으로 명시적으로 교체한다.

## 적용 순서

1. 가비아에 `grafana` A 레코드를 추가하고 DNS 전파를 확인한다.
2. EC2의 기존 Nginx sites-enabled 패턴에 아래 HTTP server block만 임시로 추가한다.
3. `sudo nginx -t`가 통과한 뒤 `sudo systemctl reload nginx`로 HTTP virtual host를 연다.
4. `sudo certbot --nginx -d grafana.allcll.kr`로 인증서를 발급한다.
5. 임시 HTTP block을 아래 최종 설정으로 교체하고, 다시 `nginx -t` 및 reload를 실행한다.
6. 이 저장소의 Compose 변경을 배포해 Grafana를 loopback으로 제한한다.
7. `https://grafana.allcll.kr` 로그인과 `/api/live/` 연결을 확인한 뒤에만 AWS에서 3000 공개 접근을 제거한다.

## 임시 HTTP 설정

```nginx
server {
    listen 80;
    listen [::]:80;
    server_name grafana.allcll.kr;

    location / {
        return 404;
    }
}
```

## 최종 HTTPS 설정

```nginx
server {
    listen 80;
    listen [::]:80;
    server_name grafana.allcll.kr;

    location / {
        return 301 https://grafana.allcll.kr$request_uri;
    }
}

server {
    listen 443 ssl;
    listen [::]:443 ssl;
    server_name grafana.allcll.kr;

    ssl_certificate /etc/letsencrypt/live/grafana.allcll.kr/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/grafana.allcll.kr/privkey.pem;

    location /api/live/ {
        proxy_pass http://127.0.0.1:3000;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_read_timeout 60s;
    }

    location / {
        proxy_pass http://127.0.0.1:3000;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 60s;
    }
}
```

## 즉시 복구

1. Grafana HTTPS 문제가 나면 Nginx의 Grafana server block을 제거하고 `sudo nginx -t && sudo systemctl reload nginx`를 실행한다.
2. Grafana가 Nginx 경유에서만 접근되는 상태라면 이번 Compose에서 변경한 포트와 Grafana 환경설정을 이전값으로 되돌린 뒤 Grafana 컨테이너만 재생성한다.
3. Grafana 데이터는 `grafana-data` Docker volume에 있으므로 이 절차에서 삭제하지 않는다.

Nginx server block 제거만으로 Grafana 컨테이너나 대시보드 데이터가 바뀌지는 않는다.
