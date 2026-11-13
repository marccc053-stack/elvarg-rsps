import gzip, struct, re
from cache317 import *
import os
ROOT=os.path.abspath(os.path.join(os.path.dirname(__file__),'..','..'))
CACHE=os.path.join(ROOT,'ElvargClient','Cache')
c=Cache(CACHE)
_v=Archive(c.read(0,5)); _mi=_v.get('map_index')
N=int.from_bytes(_mi[:2],'big')
INDEX={}
for i in range(N):
    r,t,o=struct.unpack('>HHH',_mi[2+i*6:8+i*6]); INDEX[r]=(t,o)
def load_floors():
    d=Archive(c.read(0,2)).get('flo.dat'); p=0
    res=[]
    for kind in (0,1):
        n=int.from_bytes(d[p:p+2],'big'); p+=2; L=[]
        for i in range(n):
            f={'rgb':0,'tex':-1,'occ':True,'rgb2':-1}
            while True:
                op=d[p]; p+=1
                if op==0: break
                if op==1: f['rgb']=int.from_bytes(d[p:p+3],'big'); p+=3
                elif op==2: f['tex']=d[p]; p+=1
                elif op==5: f['occ']=False
                elif op==7: f['rgb2']=int.from_bytes(d[p:p+3],'big'); p+=3
                else: raise Exception(op)
            L.append(f)
        res.append(L)
    return res
UNDER,OVER=load_floors()
def dec_terrain(data):
    # returns dict arrays [z][x][y]
    H=[[[None]*64 for _ in range(64)] for _ in range(4)]
    OV=[[[0]*64 for _ in range(64)] for _ in range(4)]
    OS=[[[0]*64 for _ in range(64)] for _ in range(4)]
    FL=[[[0]*64 for _ in range(64)] for _ in range(4)]
    UN=[[[0]*64 for _ in range(64)] for _ in range(4)]
    p=0
    for z in range(4):
        for x in range(64):
            for y in range(64):
                while True:
                    op=data[p]; p+=1
                    if op==0: break
                    if op==1: H[z][x][y]=data[p]; p+=1; break
                    if op<=49: OV[z][x][y]=data[p]; OS[z][x][y]=op-2; p+=1
                    elif op<=81: FL[z][x][y]=op-49
                    else: UN[z][x][y]=op-81
    return dict(H=H,OV=OV,OS=OS,FL=FL,UN=UN)
def enc_terrain(t):
    out=bytearray()
    for z in range(4):
        for x in range(64):
            for y in range(64):
                if t['OV'][z][x][y]: out+=bytes([t['OS'][z][x][y]+2, t['OV'][z][x][y]])
                if t['FL'][z][x][y]: out.append(t['FL'][z][x][y]+49)
                if t['UN'][z][x][y]: out.append(t['UN'][z][x][y]+81)
                h=t['H'][z][x][y]
                if h is None: out.append(0)
                else: out+=bytes([1,h])
    return bytes(out)
def _smart(d,p):
    if d[p]<128: return d[p],p+1
    return ((d[p]<<8)|d[p+1])-32768,p+2
def dec_objects(data):
    res=[]; p=0; oid=-1
    while True:
        base=0
        while True:
            v,p=_smart(data,p)
            if v==32767: base+=32767
            else: break
        inc=base+v
        if inc==0: break
        oid+=inc; loc=0
        while True:
            v,p=_smart(data,p)
            if v==0: break
            loc+=v-1
            a=data[p]; p+=1
            res.append((oid,(loc>>6)&63,loc&63,loc>>12,a>>2,a&3))
    return res
def _wsmart(v):
    return bytes([v]) if v<128 else struct.pack('>H',v+32768)
def enc_objects(objs):
    out=bytearray(); by={}
    for (oid,x,y,z,t,r) in objs: by.setdefault(oid,[]).append(((z<<12)|(x<<6)|y,(t<<2)|r))
    last=-1
    for oid in sorted(by):
        inc=oid-last; last=oid
        while inc>=32767: out+=_wsmart(32767); inc-=32767
        if inc==0: raise Exception('inc0')  # cannot happen unless exact multiple
        out+=_wsmart(inc)
        loc=0
        for l,a in sorted(by[oid]):
            out+=_wsmart(l-loc+1); loc=l; out.append(a)
        out.append(0)
    out.append(0)
    return bytes(out)
_cache={}
def region(rx,ry):
    k=(rx<<8)|ry
    if k in _cache: return _cache[k]
    if k not in INDEX: _cache[k]=None; return None
    t,o=INDEX[k]
    td=c.read(4,t); od=c.read(4,o)
    try:
        T=dec_terrain(gzip.decompress(td)) if td else None
        O=dec_objects(gzip.decompress(od)) if od else []
    except Exception as e:
        T=None;O=[]
    _cache[k]=(T,O) if T else None
    return _cache[k]
