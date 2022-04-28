FROM openjdk:8-jre
LABEL maintainer="Kang Shentu"  email="shentu.k@northeastern.edu"

USER root
RUN apt-get update -y
RUN apt-get install -y libgomp1
COPY svc /app/svc/
CMD /app/svc/bin/start -Dhttp.port=7759 -Dplay.crypto.secret=secret -Dplay.http.secret.key='QCY?tAnfk?aZ?iwrNwnxIlR6CTf:G3gf:90Latabg@5241AB`R5W:1uDFN];Ik@n'
