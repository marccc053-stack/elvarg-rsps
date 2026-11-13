import struct
import os
D=os.path.abspath(os.path.join(os.path.dirname(__file__),'..','..','ElvargServer','data','clipping'))+os.sep
dat=open(D+'loc.dat','rb').read(); idx=open(D+'loc.idx','rb').read()
n=struct.unpack('>H',idx[:2])[0]; offs=[];p=2
for j in range(n):
    offs.append(p); p+=struct.unpack('>H',idx[2+j*2:4+j*2])[0]
def rs(b,p):
    e=b.index(b'\n',p); return b[p:e].decode('latin1'),e+1
def loc(i):
    p=offs[i]; d={'id':i,'name':None,'sx':1,'sy':1,'solid':True,'acts':[],'types':None,'models':0}
    while True:
        op=dat[p]; p+=1
        if op==0: break
        if op==1:
            l=dat[p]; p+=1; d['types']=[dat[p+k*3+2] for k in range(l)]; d['models']=l; p+=l*3
        elif op==2: d['name'],p=rs(dat,p)
        elif op==5: l=dat[p]; p+=1+l*2; d['models']=l
        elif op==14: d['sx']=dat[p]; p+=1
        elif op==15: d['sy']=dat[p]; p+=1
        elif op==17: d['solid']=False
        elif op in (18,21,22,23,27,62,64,73,74): pass
        elif op in (19,28,29,39,69,75,81): p+=1
        elif op in (24,65,66,67,68,70,71,72,82): p+=2
        elif 30<=op<35: s,p=rs(dat,p); d['acts'].append(s)
        elif op in (40,41): l=dat[p]; p+=1+l*4
        elif op==78: p+=3
        elif op==79: p+=5; l=dat[p]; p+=1+l*2
        elif op in (77,92):
            p+=4
            if op==92: p+=2
            l=dat[p]; p+=1+(l+1)*2
        else: raise Exception((i,op))
    return d
LOCS=[]
for i in range(n):
    try: LOCS.append(loc(i))
    except Exception as e: LOCS.append({'id':i,'name':None,'sx':1,'sy':1,'solid':True,'acts':[],'types':None,'models':0})
