#!/usr/bin/env python3
"""JAR real + H2 em arquivo; IA via HTTP local controlado, sem chamadas pagas."""
import json,os,subprocess,sys,tempfile,time,urllib.request,urllib.error,threading
from http.server import ThreadingHTTPServer,BaseHTTPRequestHandler
from pathlib import Path
sys.stdout.reconfigure(encoding="utf-8")
jar=Path(__file__).resolve().parents[1]/"target/TOTVS-AdvisorMind-3.0.0.jar"
java=str(Path(os.environ["JAVA_HOME"])/"bin/java") if os.environ.get("JAVA_HOME") else "java"
root=Path(tempfile.mkdtemp(prefix="advisormind-final-http-"));evidence=[];process=None;phase="inicial";provider_calls=0
base="http://127.0.0.1:18080"
class Provider(BaseHTTPRequestHandler):
 def do_POST(self):
  global provider_calls
  provider_calls+=1;self.rfile.read(int(self.headers.get("Content-Length",0)))
  b=json.dumps({"model":"controlled-local-fixture","usage":{"total_tokens":77},"choices":[{"finish_reason":"stop","message":{"content":"FIXTURE DE TESTE: cliente interessado em demonstracao."}}]}).encode()
  self.send_response(200);self.send_header("Content-Type","application/json");self.send_header("Content-Length",str(len(b)));self.end_headers();self.wfile.write(b)
 def log_message(self,*args):pass
server=ThreadingHTTPServer(("127.0.0.1",0),Provider);threading.Thread(target=server.serve_forever,daemon=True).start()
opener=urllib.request.build_opener(urllib.request.ProxyHandler({}))
def call(method,path,body=None,expected=200):
 data=None if body is None else json.dumps(body,ensure_ascii=False).encode()
 req=urllib.request.Request(base+path,data=data,method=method,headers={"Content-Type":"application/json"})
 try:
  with opener.open(req,timeout=10) as r:status=r.status;raw=r.read()
 except urllib.error.HTTPError as e:status=e.code;raw=e.read()
 assert status==expected,(method,path,status,raw.decode())
 result=json.loads(raw) if raw else None;evidence.append({"fase":phase,"metodo":method,"uri":path,"requisicao":body,"status":status,"resposta":result});return result

def start(controlled=False):
 global process
 # No Windows terminate() encerra sem shutdown hook. Flush síncrono garante
 # que o teste de reinício leia commits duráveis mesmo nesse encerramento.
 args=[java,"-jar",str(jar),"--spring.datasource.url=jdbc:h2:file:./data/advisormind-final;MODE=Oracle;DB_CLOSE_ON_EXIT=FALSE;WRITE_DELAY=0"]
 if controlled:args += ["--advisormind.openai-key=test-only",f"--advisormind.openai-endpoint=http://127.0.0.1:{server.server_port}/chat"]
 env=dict(os.environ,SPRING_PROFILES_ACTIVE="local",PORT="18080",OPENAI_API_KEY="")
 process=subprocess.Popen(args,cwd=root,env=env,stdout=(root/"application.log").open("ab"),stderr=subprocess.STDOUT)
 for _ in range(150):
  if process.poll() is not None:raise RuntimeError((root/"application.log").read_text())
  try:opener.open(base+"/api/vendedores",timeout=1).close();return
  except OSError:time.sleep(.2)
 raise RuntimeError("Aplicação não iniciou")
def stop():
 global process
 if process:
  process.terminate()
  try:process.wait(timeout=15)
  except subprocess.TimeoutExpired:process.kill();process.wait()
  process=None
try:
 start()
 vr=call("POST","/api/vendedor-repos",{"filtro":"Demonstracao"},201)["id"]
 rr=call("POST","/api/reuniao-repos",{},201)["id"]
 client={"nome":"Joao Teste","empresa":"Empresa ME","dataNascimento":"1990-01-01"}
 c=call("POST","/api/clientes",client,201)["id"];call("POST","/api/clientes",client,409)
 seller={"nome":"Ana Teste","cargo":"Consultor","setor":"Tecnologia","totalReunioes":10,"vendedorRepoId":vr}
 v=call("POST","/api/vendedores",seller,201)["id"];call("POST","/api/vendedores",seller,409)
 seller["nome"]="Ana Atualizada";call("PUT",f"/api/vendedores/{v}",seller)
 call("GET",f"/api/vendedores/{v}/indicadores?ticketMedio=1500");call("GET",f"/api/vendedores/{v}/indicadores")
 meeting={"transcricao":'Cliente: "Gostei", vamos avancar.\nVendedor: proposta! ação 🚀',"data":"2026-09-01T10:00:00","status":"AGENDADA","clienteId":c,"vendedorId":v,"reuniaoRepoId":rr}
 r=call("POST","/api/reunioes",meeting,201)["id"];call("POST","/api/reunioes",meeting,409)
 call("POST","/api/reunioes",dict(meeting,vendedorId=999999),404);call("POST","/api/reunioes",dict(meeting,transcricao="x"*4001),400)
 call("GET",f"/api/reunioes/{r}/indicadores");call("GET","/api/reunioes/estatisticas")
 for tipo in ["smart-briefing","conversational-analytics","voc-tracker"]:call("POST",f"/api/reunioes/{r}/analises/{tipo}",expected=503)
 stop();phase="reinicio-com-provedor-controlado";start(True)
 assert call("GET",f"/api/reunioes/{r}")["transcricao"]==meeting["transcricao"]
 assert call("GET",f"/api/vendedores/{v}")["nome"]=="Ana Atualizada"
 aids=[]
 for tipo in ["smart-briefing","conversational-analytics","voc-tracker"]:
  a=call("POST",f"/api/reunioes/{r}/analises/{tipo}",expected=201);assert a["tokensUtilizados"]==77;assert a["modeloIa"]=="controlled-local-fixture";aids.append(a["id"])
 n=provider_calls;call("POST",f"/api/reunioes/{r}/analises/smart-briefing",expected=409);assert provider_calls==n
 assert call("POST",f"/api/reunioes/{r}/analises/smart-briefing/previa")["persistida"] is False
 stop();phase="segundo-reinicio-verificacao-relacional";start()
 for resource,id in [("vendedor-repos",vr),("reuniao-repos",rr),("clientes",c),("vendedores",v),("reunioes",r),("analises",aids[0])]:assert call("GET",f"/api/{resource}/{id}")["id"]==id
 assert len(call("GET",f"/api/reunioes/{r}/analises"))==3
 assert call("GET",f"/api/reuniao-repos/{rr}")["totalReunioes"]==1
 assert call("GET",f"/api/vendedor-repos/{vr}")["totalVendedores"]==1
 call("PUT",f"/api/reunioes/{r}",dict(meeting,transcricao="Alteracao bloqueada por analise"),409)
 for resource,id in [("reunioes",r),("vendedores",v),("clientes",c),("vendedor-repos",vr),("reuniao-repos",rr)]:call("DELETE",f"/api/{resource}/{id}",expected=409)
 for a in aids:call("DELETE",f"/api/analises/{a}",expected=204)
 meeting["status"]="ATENDIDA";call("PUT",f"/api/reunioes/{r}",meeting)
 call("DELETE",f"/api/reunioes/{r}",expected=204);assert call("GET",f"/api/reuniao-repos/{rr}")["totalReunioes"]==0
 call("DELETE",f"/api/vendedores/{v}",expected=204);assert call("GET",f"/api/vendedor-repos/{vr}")["totalVendedores"]==0
 for resource,id in [("clientes",c),("vendedor-repos",vr),("reuniao-repos",rr)]:call("DELETE",f"/api/{resource}/{id}",expected=204)
 call("GET",f"/api/reunioes/{r}",expected=404)
 print(json.dumps({"resultado":"PASSOU","verificacoes_http":len(evidence),"inicializacoes":3,"fonte_ia":"Servidor HTTP local controlado; nao e OpenAI real.","chamadas_ao_provedor_controlado":provider_calls,"oracle_real":"NAO EXECUTADO","openai_real":"NAO EXECUTADO","evidencias":evidence},ensure_ascii=False,indent=2))
finally:stop();server.shutdown();server.server_close()
