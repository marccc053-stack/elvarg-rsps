from PIL import Image, ImageDraw
from maplib import UNDER, OVER
TEXCOL={1:(60,100,170),3:(120,90,50),15:(90,60,40),25:(50,80,60),31:(230,110,20),24:(60,100,170)}
def tile_rgb(un,ov):
    if ov:
        f=OVER[ov-1]
        if f['tex']>=0: return TEXCOL.get(f['tex'],(150,0,150))
        if f['rgb']==0xff00ff: return (20,20,20) if f['rgb2']==-1 else ((f['rgb2']>>16)&255,(f['rgb2']>>8)&255,f['rgb2']&255)
        return ((f['rgb']>>16)&255,(f['rgb']>>8)&255,f['rgb']&255)
    if un:
        r=UNDER[un-1]['rgb']; return ((r>>16)&255,(r>>8)&255,r&255)
    return (0,0,0)
def render(get, x0,y0,w,h,s=8,plane=0,grid=None,labels=None):
    """get(x,y)->(T tile dict access fn) ; get returns (un,ov,height,objs list[(id,type,rot,z)])"""
    im=Image.new('RGB',(w*s,h*s)); d=ImageDraw.Draw(im)
    for x in range(w):
        for y in range(h):
            t=get(x0+x,y0+y)
            px,py=x*s,(h-1-y)*s
            if t is None: continue
            un,ov,hh,objs=t
            col=tile_rgb(un,ov)
            if hh is not None and not (ov and OVER[ov-1]['tex']>=0):
                k=0.75+min(hh,120)/240; col=tuple(min(255,int(v*k)) for v in col)
            d.rectangle([px,py,px+s-1,py+s-1],fill=col)
            for (oid,ty,rot,z) in objs:
                if z!=plane: continue
                if ty in (0,2):
                    for rr in ([rot] if ty==0 else [rot,(rot+1)&3]):
                        if rr==0: d.line([px,py,px,py+s-1],fill=(255,255,255))
                        if rr==1: d.line([px,py,px+s-1,py],fill=(255,255,255))
                        if rr==2: d.line([px+s-1,py,px+s-1,py+s-1],fill=(255,255,255))
                        if rr==3: d.line([px,py+s-1,px+s-1,py+s-1],fill=(255,255,255))
                elif ty in (1,3): d.point([px+s//2,py+s//2],fill=(255,255,255))
                elif ty==9: d.line([px,py,px+s-1,py+s-1],fill=(255,255,255))
                elif ty in (10,11): d.rectangle([px+s//2-1,py+s//2-1,px+s//2+1,py+s//2+1],fill=(255,0,255) if s>4 else (255,0,255))
                elif ty==22 and s>=8: d.point([px+s//2,py+s//2],fill=(255,255,0))
    if grid:
        for x in range(w):
            if (x0+x)%grid==0:
                d.line([x*s,0,x*s,h*s],fill=(255,0,0)); d.text((x*s+2,2),str(x0+x),fill=(255,255,0))
        for y in range(h):
            if (y0+y)%grid==0:
                d.line([0,(h-1-y)*s+s-1,w*s,(h-1-y)*s+s-1],fill=(255,0,0)); d.text((2,(h-1-y)*s-10),str(y0+y),fill=(255,255,0))
    return im
